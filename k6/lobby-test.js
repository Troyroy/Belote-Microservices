import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const errorRate = new Rate('errors');

export const options = {
    stages: [
        { duration: '30s', target: 15 },
        { duration: '2m', target: 75 },
        { duration: '1m', target: 75 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<400', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8083';

export function setup() {
    // Get auth token
    const loginPayload = JSON.stringify({
        email: 'georgievtroantest@gmail.com',
        password: '310803gg',
    });

    const params = { headers: { 'Content-Type': 'application/json' } };
    const loginRes = http.post('http://localhost:8082/users/login', loginPayload, params);

    return { token: loginRes.json('token') || null };
}

export default function (data) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': data.token ? `Bearer ${data.token}` : '',
        },
    };

    group('Lobby Operations', function () {
        // Get active lobbies

        if (data.token) {
            // Create lobby
            const createPayload = JSON.stringify({
                lobbyName: `Lobby_${__VU}_${__ITER}`,
                hostId: 1
            });

            let res = http.post(`${BASE_URL}/lobby`, createPayload, params);
            const createSuccess = check(res, {
                'create lobby status is 201': (r) => r.status === 201 || r.status === 200,
            });

            // if (createSuccess && res.json('id')) {
            //     const lobbyId = res.json('id');
            //
            //     sleep(2);
            //
            //     // Get lobby details
            //     res = http.get(`${BASE_URL}/api/lobbies/${lobbyId}`, params);
            //     check(res, {
            //         'lobby detail status is 200': (r) => r.status === 200,
            //     });
            //
            //     sleep(1);
            //
            //     // Leave/Delete lobby
            //     res = http.del(`${BASE_URL}/api/lobbies/${lobbyId}`, null, params);
            //     check(res, {
            //         'delete lobby success': (r) => r.status === 200 || r.status === 204,
            //     });
            // }
        }

        sleep(2);
    });

    sleep(Math.random() * 2 + 1);
}

export function handleSummary(data) {
    return {
        "./scripts/lobby-summary.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}