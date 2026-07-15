import http from 'k6/http';
import { check, fail } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const username = __ENV.USERNAME || 'admin';
const password = __ENV.PASSWORD || '123456';

export const options = {
  scenarios: {
    order_list: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 20),
      timeUnit: '1s',
      duration: __ENV.DURATION || '30s',
      preAllocatedVUs: 20,
      maxVUs: 80,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    checks: ['rate>0.99'],
  },
};

export function setup() {
  const response = http.post(
    `${baseUrl}/admin/users/login`,
    JSON.stringify({ username, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  const loginSucceeded = check(response, {
    '登录接口返回 200': (res) => res.status === 200,
    '登录业务码为 200': (res) => res.json('code') === 200,
    '登录响应包含 token': (res) => Boolean(res.json('data.token')),
  });

  if (!loginSucceeded) {
    fail(`登录失败: HTTP ${response.status}, body=${response.body}`);
  }

  return { token: response.json('data.token') };
}

export default function (data) {
  const response = http.get(
    `${baseUrl}/admin/service-orders?page=1&pageSize=20`,
    {
      headers: { token: data.token },
      tags: { api: 'order-list' },
    },
  );

  check(response, {
    '订单列表 HTTP 状态为 200': (res) => res.status === 200,
    '订单列表业务码为 200': (res) => res.json('code') === 200,
    '订单列表返回数据': (res) => Array.isArray(res.json('data.records')),
  });
}
