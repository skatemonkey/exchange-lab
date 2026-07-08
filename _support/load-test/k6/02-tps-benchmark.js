import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EXCHANGE_URL = __ENV.EXCHANGE_URL || 'http://localhost:8080';
const FINANCE_URL = __ENV.FINANCE_URL || 'http://localhost:8081';
const MATCH_URL = __ENV.MATCH_URL || 'http://localhost:8082';

const RATE = Number(__ENV.RATE || 100);
const DURATION = __ENV.DURATION || '30s';
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 50);
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const BUYER_COUNT = Number(__ENV.BUYER_COUNT || 100);
const DRAIN_SECONDS = Number(__ENV.DRAIN_SECONDS || 5);

const acceptedOrders = new Counter('accepted_orders');
const orderApiDuration = new Trend('order_api_duration');

const SERVICE_METRICS = [
  {
    label: 'Intake TPS',
    source: EXCHANGE_URL,
    name: 'exchange.orders.accepted',
  },
  {
    label: 'Reservation TPS',
    source: FINANCE_URL,
    name: 'finance.reservations.completed',
  },
  {
    label: 'Match TPS',
    source: MATCH_URL,
    name: 'match.orders.processed',
  },
  {
    label: 'Trades Created TPS',
    source: MATCH_URL,
    name: 'match.trades.created',
  },
  {
    label: 'Settlement TPS / End-to-End TPS',
    source: FINANCE_URL,
    name: 'finance.settlements.completed',
  },
];

export const options = {
  scenarios: {
    order_intake: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: DURATION,
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
    },
  },
};

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function uuidFromNumber(value) {
  return `00000000-0000-0000-0000-${String(value).padStart(12, '0')}`;
}

function buyerId() {
  return uuidFromNumber(randomInt(1, BUYER_COUNT));
}

function durationSeconds(value) {
  const match = String(value).match(/^(\d+)(s|m|h)$/);
  if (!match) {
    return 0;
  }

  const amount = Number(match[1]);
  const unit = match[2];
  if (unit === 'h') {
    return amount * 60 * 60;
  }
  if (unit === 'm') {
    return amount * 60;
  }
  return amount;
}

function metricCount(source, name) {
  const response = http.get(`${source}/actuator/metrics/${name}`);
  if (response.status !== 200) {
    return null;
  }

  const body = response.json();
  for (const measurement of body.measurements || []) {
    if (measurement.statistic === 'COUNT') {
      return Number(measurement.value);
    }
  }

  return null;
}

function readMetrics() {
  const values = {};
  for (const metric of SERVICE_METRICS) {
    values[metric.name] = metricCount(metric.source, metric.name);
  }
  return values;
}

function printMetricReport(before, after, seconds) {
  console.log('\nTPS counter report');
  console.log(`Duration: ${DURATION}`);
  console.log(`Target rate: ${RATE} orders/sec`);
  console.log(`Drain wait: ${DRAIN_SECONDS}s`);

  for (const metric of SERVICE_METRICS) {
    const start = before[metric.name];
    const end = after[metric.name];
    if (start === null || end === null) {
      console.log(`- ${metric.label}: unavailable (${metric.name})`);
      continue;
    }

    const count = end - start;
    const tps = seconds > 0 ? count / seconds : 0;
    console.log(`- ${metric.label}: count=${count}, tps=${tps.toFixed(2)}`);
  }
}

export function setup() {
  return {
    metrics: readMetrics(),
  };
}

export default function () {
  const payload = JSON.stringify({
    traderId: buyerId(),
    symbol: 'ACME',
    side: 'BUY',
    limitPrice: '100.00000000',
    quantity: '1.00000000',
  });

  const response = http.post(`${BASE_URL}/api/orders/limit`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  orderApiDuration.add(response.timings.duration);

  const accepted = check(response, {
    'status is 202': (r) => r.status === 202,
  });

  if (accepted) {
    acceptedOrders.add(1);
  }
}

export function teardown(data) {
  if (DRAIN_SECONDS > 0) {
    sleep(DRAIN_SECONDS);
  }

  printMetricReport(data.metrics, readMetrics(), durationSeconds(DURATION));
}
