create table context_problem
(
    id         bigint auto_increment
        primary key,
    problem_id bigint not null,
    context_id bigint not null
);

INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (1, 14, 1);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (2, 15, 1);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (3, 16, 1);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (4, 17, 1);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (5, 22, 2);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (6, 23, 2);
INSERT INTO polinoj.context_problem (id, problem_id, context_id) VALUES (7, 24, 2);