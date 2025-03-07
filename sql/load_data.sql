-- TRRUNCATE TABLE mieux que DELETE FROM car pas besoin de faire un ALTER TABLE ... AUTO_INCREMENT = 1 pour que l'incrément automatique reparte à 1.
-- Désactive temporairement la vérification des clés étrangères
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
TRUNCATE TABLE connections;
TRUNCATE TABLE transactions;
-- Réactive la vérification des clés étrangères avant toutes insertions pour contrôle de l'intégrité.
SET FOREIGN_KEY_CHECKS = 1;

-- hashed password generated by https://bcrypt-generator.com/ (le mot de passe étant le prénom pour les tests).
INSERT INTO users (role, name, email, password, balance) VALUES
('ADMIN', 'Laurent', 'laurent@orange.fr', '$2a$12$AaBJmcAXPU6lnXVRzsZYC.YBXiwNv4BzgvnOf5TGEasgALze41xKO', 10.00),
('USER', 'Isabelle', 'isabelle@gmail.com', '$2a$12$nXeVAizBHUNU/aXHwQ5nfe4qbujwtUCmcV50p2/0F62B6yhSYYVNq', 150.00),
('USER', 'Satine', 'satine@chatetchien.com', '$2a$12$vdkn8fr6FzsXxO77DIvEJ.jnnq39xzI0IWtbAf.JLeoFqpcZZMQqi', 123.21),
('USER', 'Chopin', 'chopin@chatetchien.com', '$2a$12$NXrIAlOBID8Te.r8qCg3ru3LJkirEmrskNiAV9DmShFAZU1U5Jnpi', 1000.00);

INSERT INTO connections (user_id, friend_id) VALUES
(1, 2), -- Laurent est ami avec Isabelle
(1, 3), -- Laurent avec Satine
(1, 4), -- Laurent avec Chopin
(2, 1), -- Isabelle avec Laurent.
(3, 1), -- Satine avec Laurent.
(3, 4), -- Satine avec Chopin.
(4, 3); -- Chopin avec Satine.

INSERT INTO transactions (sender_id, receiver_id, description, amount) VALUES
(2, 1, 'Remboursement achat de boisson pour la fête de samedi', 22.75),
(1, 2, 'Prêt', 100.78),
(4, 3, 'Rembousement du sac de croquettes', 55.00);


