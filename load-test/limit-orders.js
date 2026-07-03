import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || '30s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BUYER_COUNT = Number(__ENV.BUYER_COUNT || 100);
const SELLER_COUNT = Number(__ENV.SELLER_COUNT || 100);

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function uuidFromNumber(value) {
  return `00000000-0000-0000-0000-${String(value).padStart(12, '0')}`;
}

function buyerId() {
  return uuidFromNumber(randomInt(1, BUYER_COUNT));
}

function sellerId() {
  return uuidFromNumber(1000 + randomInt(1, SELLER_COUNT));
}

export default function () {
  const isBuy = Math.random() < 0.5;
  const payload = JSON.stringify({
    traderId: isBuy ? buyerId() : sellerId(),
    symbol: 'ACME',
    side: isBuy ? 'BUY' : 'SELL',
    limitPrice: '100.00000000',
    quantity: '1.00000000',
  });

  const response = http.post(`${BASE_URL}/api/orders/limit`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(response, {
    'status is 202': (r) => r.status === 202,
  });

  sleep(0.05);
}
