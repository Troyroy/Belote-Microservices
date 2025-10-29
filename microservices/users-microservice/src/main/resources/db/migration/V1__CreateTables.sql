CREATE TABLE `users`
(
    id           int         NOT NULL AUTO_INCREMENT,
    username     varchar(20) NOT NULL,
    password     varchar(255) NOT NULL,
    email        varchar(50) NOT NULL,
    role        varchar(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE `games`
(
    id           int         NOT NULL AUTO_INCREMENT,
    player1      int,
    player2      int,
    player3      int,
    player4      int,
    team1Score int NOT NULL ,
    team2Score int NOT NULL ,
    winner int NOT NULL ,
    PRIMARY KEY (id),
    FOREIGN KEY (player1) REFERENCES users (id),
    FOREIGN KEY (player2) REFERENCES users (id),
    FOREIGN KEY (player3) REFERENCES users (id),
    FOREIGN KEY (player4) REFERENCES users (id)
);


