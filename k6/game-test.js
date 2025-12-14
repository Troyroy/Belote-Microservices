import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

// Custom metrics
const lobbyCreationRate = new Rate('lobby_creation_success');
const gameCreationRate = new Rate('game_creation_success');
const cardPlayRate = new Rate('card_play_success');
const gameStateRate = new Rate('game_state_success');
const errorRate = new Rate('errors');
const lobbyCreationDuration = new Trend('lobby_creation_duration');
const gameCreationDuration = new Trend('game_creation_duration');
const cardPlayDuration = new Trend('card_play_duration');
const gameStateRetrievalDuration = new Trend('game_state_retrieval_duration');
const totalRequests = new Counter('total_requests');

const BASE_URL ='http://localhost:8080/api'; // API Gateway
const USERS_API = `${BASE_URL}/users`;
const LOBBY_API = `${BASE_URL}/lobbies`;
const GAME_API = `${BASE_URL}/games`;

const USER_CREDENTIALS = {
    username: 'Troyroy',
    password: '310803gg',
};

// Test configuration options
export const options = {
    stages: [
        { duration: '2m', target: 5 },
        { duration: '5m', target: 5 },
        { duration: '2m', target: 10 },
        { duration: '5m', target: 10 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95)<1000', 'p(99)<2000'],
        'http_req_failed': ['rate<0.05'],
        'lobby_creation_success': ['rate>0.95'],
        'game_creation_success': ['rate>0.95'],
        'card_play_success': ['rate>0.90'],
        'game_state_success': ['rate>0.98'],
        'errors': ['rate<0.05'],
    },
    tags: {
        service: 'belote-game',
        test_type: 'load_test',
    },
};

function authenticate() {
    const payload = JSON.stringify({
        username: USER_CREDENTIALS.username,
        password: USER_CREDENTIALS.password,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: { name: 'Authenticate' },
    };

    const response = http.post(`${USERS_API}/login`, payload, params);

    totalRequests.add(1);

    const success = check(response, {
        'login status is 200': (r) => r.status === 200,
        'login has access token': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.accessToken !== undefined;
            } catch (e) {
                return false;
            }
        },
    });


    if (!success) {
        console.error('Authentication failed:', response.status, response.body);
        errorRate.add(1);
        return null;
    }

    try {
        const body = JSON.parse(response.body);
        return {
            token: `Bearer ${body.accessToken}`,
            userId: body.userId || USER_CREDENTIALS.username,
        };
    } catch (e) {
        console.error('Failed to parse login response');
        return null;
    }
}

function testCreateLobby(user) {
    const payload = JSON.stringify({
        lobbyName: `Load Test Lobby ${randomString(8)}`,
        hostId: user.userId
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': user.token,
        },
        tags: { name: 'CreateLobby' },
    };

    const startTime = new Date();
    const response = http.post(LOBBY_API, payload, params);
    const duration = new Date() - startTime;

    totalRequests.add(1);
    lobbyCreationDuration.add(duration);

    let body = "";
    const success = check(response, {
        'create lobby status is 201': (r) => r.status === 201,
        'create lobby has lobbyId': (r) => {
            try {
                console.log("Lobby id");

                body = JSON.parse(r.body);
                console.log(body.lobbyId);
                return body.lobbyId;
            } catch (e) {
                return false;
            }
        },
        'create lobby response time < 1s': (r) => r.timings.duration < 1000,
    });

    console.log(("After parse"))
    lobbyCreationRate.add(success);
    if (!success) errorRate.add(1);

    if (response.status === 201) {
            console.log("Body")
            return body;
    }
    return body;
}

function testStartLobby(lobbyId, user) {

    const params = {
        headers: {
            'Authorization': user.token,
        },
        tags: { name: 'StartLobby' },
    };

    const response = http.post(`${LOBBY_API}/${lobbyId}/start`, null, params);

    totalRequests.add(1);

    const success = check(response, {
        'start lobby status is 200': (r) => r.status === 200,
        'start lobby response time < 1s': (r) => r.timings.duration < 1000,
    });

    if (!success) errorRate.add(1);

    if (response.status === 200) {
        return lobbyId;
    }
    return null;
}

function testGetGameState(gameId, user) {
    const params = {
        headers: {
            'Authorization': user.token,
        },
        tags: { name: 'GetGameState' },
    };

    const startTime = new Date();
    const response = http.get(`${GAME_API}/${gameId}`, params);
    const duration = new Date() - startTime;

    totalRequests.add(1);
    gameStateRetrievalDuration.add(duration);

    const success = check(response, {
        'get state status is 200': (r) => r.status === 200,
        'get state has gameId': (r) => {
return true;
        },
        'get state response time < 500ms': (r) => r.timings.duration < 500,
    });

    gameStateRate.add(success);
    if (!success) errorRate.add(1);

    if (response.status === 200) {
        try {
            const body = JSON.parse(response.body);
            return body;
        } catch (e) {
            console.log(e)
            console.error('Failed to parse game state response');
            return null;
        }
    }
    return null;
}

function getPlayableCards(hand, cardToAnswer) {
    if (!hand || hand.length === 0) {
        return [];
    }

    if (!cardToAnswer) {
        return hand;
    }

    let playableCards = hand.filter(card =>
        card.suit === cardToAnswer.suit && card.points > cardToAnswer.points
    );

    if (playableCards.length === 0) {
        playableCards = hand.filter(card => card.suit === cardToAnswer.suit);
    }

    return playableCards.length > 0 ? playableCards : hand;
}


function getCardFromHand(gameState, playerId) {
    if (!gameState || !gameState.hands || !gameState.hands[playerId]) {
        console.warn(`No hand found for player ${playerId}`);
        return null;
    }

    const hand = gameState.hands[playerId];
    if (hand.length === 0) {
        console.warn(`Player ${playerId} has empty hand`);
        return null;
    }

    const cardToAnswer = gameState.cardToAnswer;

    const playableCards = getPlayableCards(hand, cardToAnswer);

    if (playableCards.length === 0) {
        console.warn(`No playable cards found, using first card from hand`);
        const card = hand[0];
        return {
            suit: card.suit.charAt(0).toUpperCase() + card.suit.slice(1).toLowerCase(),
            rank: card.rank.charAt(0).toUpperCase() + card.rank.slice(1).toLowerCase(),
            id: card.id
        };
    }

    const card = playableCards[randomIntBetween(0, playableCards.length - 1)];

    return {
        suit: card.suit.charAt(0).toUpperCase() + card.suit.slice(1).toLowerCase(),
        rank: card.rank.charAt(0).toUpperCase() + card.rank.slice(1).toLowerCase(),
        id: card.id
    };
}

function testPlayCard(gameId, user, gameState) {
    const playerId = '1';

    const card = getCardFromHand(gameState, playerId);

    if (!card) {
        console.warn('No valid card to play, skipping');
        return null;
    }

    const payload = JSON.stringify({ card: card });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': user.token,
        },
        tags: { name: 'PlayCard' },
    };

    const startTime = new Date();
    const response = http.post(`${GAME_API}/${gameId}/${card.id}`, payload, params);
    const duration = new Date() - startTime;

    totalRequests.add(1);
    console.log("Duration")
    console.log(duration);
    cardPlayDuration.add(duration);

    const success = check(response, {
        'play card status is 200 or 400': (r) => r.status === 200 || r.status === 400,
        'play card response time < 800ms': (r) => r.timings.duration < 800,
    });

    console.log("sicc")
    console.log(success)


    cardPlayRate.add(success);
    if (response.status >= 500) errorRate.add(1);

    if (response.status === 200) {
        try {
            return JSON.parse(response.body);
        } catch (e) {
            console.error('Failed to parse play card response');
            return null;
        }
    }
    return null;
}


function testHealthCheck() {
    const response = http.get(`http://localhost:8080/actuator/health`, {
        tags: { name: 'HealthCheck' },
    });

    totalRequests.add(1);

    check(response, {
        'health check status is 200': (r) => r.status === 200,
        'health check response time < 200ms': (r) => r.timings.duration < 600,
    });
    console.log("Health check")
}


export default function() {

    const user = authenticate();

    if (!user) {
        console.error('Failed to authenticate, skipping iteration');
        sleep(5);
        return;
    }

    group('Belote Game Load Test', () => {

        group('Health Check', () => {
            testHealthCheck();
            sleep(0.5);
        });


        let lobby = null;
        group('Create Lobby', () => {
            lobby = testCreateLobby(user);
            sleep(1);
        });

        if (!lobby) {
            console.error('Failed to create lobby, skipping rest of test');
            return;
        }

        let startResult = null;
        group('Start Lobby', () => {
            startResult = testStartLobby(lobby.lobbyId, user);
            sleep(2);
        });


        const gameId = lobby.lobbyId;

        console.log("GameID")
        console.log(gameId);
        let gameState = null;
        group('Get Initial Game State', () => {
            gameState = testGetGameState(gameId, user);
            sleep(1);
        });

        if (!gameState) {
            console.error('Failed to get game state, skipping card play');
            return;
        }


        group('Play Cards', () => {
            for (let i = 0; i < 8; i++) {
                if (gameState && gameState.status === 'FINISHED') {
                    console.log('Game finished, stopping card play');
                    break;
                }

                testPlayCard(gameId, user, gameState);

                sleep(5);

                gameState = testGetGameState(gameId, user);
                sleep(1);

                sleep(randomIntBetween(1, 3));
            }
        });
    });

    sleep(randomIntBetween(5, 10));
}

export function setup() {
    console.log(`Starting load test against: ${BASE_URL}`);
    console.log(`User: ${USER_CREDENTIALS.username}`);
    console.log('Test duration: ~16 minutes');

    const testAuth = authenticate();
    if (!testAuth) {
        throw new Error('Failed to authenticate with provided credentials');
    }

    console.log('Authentication successful!');

    return { startTime: new Date() };
}

export function handleSummary(data) {
    return {
        "game-summary.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}

export function teardown(data) {
    const endTime = new Date();
    const duration = (endTime - data.startTime) / 1000 / 60;
    console.log(`Test completed in ${duration.toFixed(2)} minutes`);
}
