import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
    stages: [
        { duration: '30s', target: 10 },  // Warm up
        { duration: '1m', target: 50 },   // Normal load
        { duration: '2m', target: 50 },   // Sustained load
        { duration: '30s', target: 100 }, // Spike
        { duration: '30s', target: 0 },   // Cool down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
        errors: ['rate<0.1'],
    },
};

const BASE_URL = 'http://users-service:8080';

export function setup() {
    // Create a test user for authentication
    const registerPayload = JSON.stringify({
        username: `testuser_${Date.now()}`,
        email: `test_${Date.now()}@example.com`,
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
        // Register new user
        const registerPayload = JSON.stringify({
            username: `user_${__VU}_${__ITER}`,
            email: `user_${__VU}_${__ITER}@test.com`,
            password: 'Password123!',
        });

        let res = http.post(`${BASE_URL}/users`, registerPayload, params);
        const registerSuccess = check(res, {
            'register status is 201': (r) => r.status === 201 || r.status === 200,
        });
        errorRate.add(!registerSuccess);

        sleep(1);

        // Login
        const loginPayload = JSON.stringify({
            email: `user_${__VU}_${__ITER}@test.com`,
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