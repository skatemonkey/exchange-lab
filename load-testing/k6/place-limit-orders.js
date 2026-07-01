import http from 'k6/http';
import { check, fail } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:9000';
const symbol = __ENV.SYMBOL || 'ACME';
const buyerCount = Number(__ENV.BUYER_COUNT || '1000');
const limitPrice = Number(__ENV.LIMIT_PRICE || '100');
const quantity = Number(__ENV.QUANTITY || '1');
const targetRate = Number(__ENV.TARGET_RATE || '50');
const startRate = Number(__ENV.START_RATE || '10');
const preAllocatedVus = Number(__ENV.PREALLOCATED_VUS || '50');
const maxVus = Number(__ENV.MAX_VUS || '200');
const maxErrorRate = Number(__ENV.MAX_ERROR_RATE || '0.20');
const p95Ms = Number(__ENV.P95_MS || '2000');

export const options = {
  scenarios: {
    place_limit_orders: {
      executor: 'ramping-arrival-rate',
      startRate,
      timeUnit: '1s',
      preAllocatedVUs: preAllocatedVus,
      maxVUs: maxVus,
      stages: [
        { target: targetRate, duration: __ENV.RAMP_UP || '30s' },
        { target: targetRate, duration: __ENV.DURATION || '1m' },
        { target: 0, duration: __ENV.RAMP_DOWN || '10s' },
      ],
    },
  },
  thresholds: {
    http_req_failed: [`rate<${maxErrorRate}`],
    http_req_duration: [`p(95)<${p95Ms}`],
  },
};

export function setup() {
  const response = http.get(`${baseUrl}/actuator/health`);
  const healthy = check(response, {
    'gateway is reachable': (res) => res.status === 200,
  });

  if (!healthy) {
    fail(`gateway health check failed status=${response.status} body=${response.body}`);
  }
}

export default function () {
  const buyerId = seededBuyerId(randomInt(1, buyerCount));
  const payload = JSON.stringify({
    traderId: buyerId,
    symbol,
    side: 'BUY',
    limitPrice,
    quantity,
  });

  const response = http.post(`${baseUrl}/api/orders/limit`, payload, {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  const accepted = check(response, {
    'order accepted': (res) => res.status === 202,
  });

  if (!accepted) {
    console.error(`order rejected status=${response.status} body=${response.body}`);
  }
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function seededBuyerId(sequence) {
  return `00000000-0000-0000-0000-${String(sequence).padStart(12, '0')}`;
}
