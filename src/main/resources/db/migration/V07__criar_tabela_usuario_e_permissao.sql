CREATE SEQUENCE IF NOT EXISTS usuario_sequence;

CREATE TABLE IF NOT EXISTS usuario (
	
	codigo INT8 NOT NULL,
	nome VARCHAR(50) NOT NULL,
	email VARCHAR(50) NOT NULL,
	senha VARCHAR(150) NOT NULL,
	
	CONSTRAINT pk_usuario PRIMARY KEY (codigo)
	
);

CREATE TABLE IF NOT EXISTS permissao (
	
	codigo INT8 NOT NULL,
	descricao VARCHAR(50) NOT NULL,
	
	CONSTRAINT pk_permisssao PRIMARY KEY (codigo)
	
);

CREATE TABLE IF NOT EXISTS usuario_permissao (
	
	codigo_usuario INT8 NOT NULL,
	codigo_permissao INT8 NOT NULL,
	
	CONSTRAINT pk_usuario_permissao PRIMARY KEY (codigo_usuario, codigo_permissao),
	CONSTRAINT fk_usuario_permissao_usuario FOREIGN KEY (codigo_usuario) REFERENCES usuario (codigo),
	CONSTRAINT fk_usuario_permissao_permissao FOREIGN KEY (codigo_permissao) REFERENCES permissao (codigo)
	 
);

ALTER TABLE usuario ALTER COLUMN codigo SET DEFAULT NEXTVAL('usuario_sequence');