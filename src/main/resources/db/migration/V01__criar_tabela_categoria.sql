CREATE TABLE IF NOT EXISTS categoria (
	codigo INT NOT NULL,
	nome VARCHAR(50) NOT NULL,
	CONSTRAINT pk_codigo PRIMARY KEY (codigo)
);

CREATE SEQUENCE IF NOT EXISTS categoria_sequence;

ALTER TABLE categoria ALTER COLUMN codigo SET DEFAULT NEXTVAL('categoria_sequence');