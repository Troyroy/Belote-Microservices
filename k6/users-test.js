import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const errorRate = new Rate('errors');

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
        errors: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8082';

export function setup() {
    // Create a test user for authentication
    const registerPayload = JSON.stringify({
        username: `testuser2_${Date.now()}`,
        email: `test2_${Date.now()}@example.com`,
        password: 'TestPassword123!',
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const registerRes = http.post(`${BASE_URL}/users`, registerPayload, params);

    if (registerRes.status === 201 || registerRes.status === 200) {
        // Login to get token
        const loginPayload = JSON.stringify({
            username: JSON.parse(registerPayload).username,
            password: 'TestPassword123!',
        });

        const loginRes = http.post(`${BASE_URL}/users/login`, loginPayload, params);

        if (loginRes.status === 200 && loginRes.json('token')) {
            return { token: loginRes.json('token') };
        }
    }

    return { token: null };
}

export default function (data) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
            //'Authorization': data.token ? `Bearer ${data.token}` : '',
        },
    };

    group('User Authentication Flow', function () {
        const registerPayload = JSON.stringify({
            username: `user2_${__VU}_${__ITER}`,
            email: `user2_${__VU}_${__ITER}@test.com`,
            password: 'Password123!',
        });

        let res = http.post(`${BASE_URL}/users`, registerPayload, params);
        const registerSuccess = check(res, {
            'register status is 201': (r) => r.status === 201 || r.status === 200,
        });
        errorRate.add(!registerSuccess);

        sleep(1);

        const loginPayload = JSON.stringify({
            email: `user2_${__VU}_${__ITER}@test.com`,
            password: 'Password123!',
        });

        res = http.post(`${BASE_URL}/users/login`, loginPayload, params);
        const loginSuccess = check(res, {
            'login status is 200': (r) => r.status === 200,
            'has token': (r) => r.json('token') !== undefined,
        });
        errorRate.add(!loginSuccess);

        sleep(1);
    });


    sleep(Math.random() * 2 + 1);
}
export function handleSummary(data) {
    return {
        "./scripts/summary.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}