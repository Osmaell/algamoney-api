CREATE TABLE IF NOT EXISTS contato (
	
	codigo INT8 NOT NULL,
	nome VARCHAR(50) NOT NULL,
	email VARCHAR(100) NOT NULL,
	telefone VARCHAR(20) NOT NULL,
	codigo_pessoa INT8 NOT NULL,
	
	CONSTRAINT pk_contato PRIMARY KEY (codigo),
	CONSTRAINT fk_contato_pessoa FOREIGN KEY (codigo_pessoa) REFERENCES pessoa (codigo)
	
);

CREATE SEQUENCE IF NOT EXISTS contato_sequence;
ALTER TABLE contato ALTER COLUMN codigo SET DEFAULT NEXTVAL('contato_sequence');