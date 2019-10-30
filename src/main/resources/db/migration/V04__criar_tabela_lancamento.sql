CREATE SEQUENCE IF NOT EXISTS lancamento_sequence;

CREATE TABLE lancamento (

	codigo INT8 NOT NULL,
	descricao VARCHAR(50) NOT NULL,
	data_vencimento DATE NOT NULL,
	data_pagamento DATE,
	valor DECIMAL(10,2) NOT NULL,
	observacao VARCHAR(100),
	tipo VARCHAR(20) NOT NULL,
	codigo_categoria INT8 NOT NULL,
	codigo_pessoa INT8 NOT NULL,
	
	CONSTRAINT pk_lancamento PRIMARY KEY (codigo),
	CONSTRAINT fk_lancamento_categoria FOREIGN KEY (codigo_categoria) REFERENCES categoria (codigo),
	CONSTRAINT fk_lancamento_pessoa FOREIGN KEY (codigo_pessoa) REFERENCES pessoa (codigo)
	
);

ALTER TABLE lancamento ALTER COLUMN codigo SET DEFAULT NEXTVAL('lancamento_sequence');