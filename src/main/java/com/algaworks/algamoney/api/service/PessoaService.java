package com.algaworks.algamoney.api.service;
	
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.algaworks.algamoney.api.model.Pessoa;
import com.algaworks.algamoney.api.repository.PessoaRepository;
import com.algaworks.algamoney.api.repository.filter.PessoaFilter;
	
@Service
public class PessoaService {
	
	@Autowired
	public PessoaRepository pessoaRepository;
	
	public Pessoa salvar(Pessoa pessoa) {
		pessoa.getContatos().forEach(c -> c.setPessoa(pessoa));
		return pessoaRepository.save(pessoa);
	}
	
	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		Pessoa pessoaSalva = buscarPeloCodigo(codigo);
		
		// trabalhando com a lista de contatos persistentes
		pessoaSalva.getContatos().clear();
		pessoaSalva.getContatos().addAll( pessoa.getContatos() );
		pessoaSalva.getContatos().forEach(c -> c.setPessoa(pessoaSalva));
		
		BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo", "contatos");
		return pessoaRepository.save(pessoaSalva);
	}
	
	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		Pessoa pessoaSalva = buscarPeloCodigo(codigo);
		pessoaSalva.setAtivo(ativo);
		pessoaRepository.save(pessoaSalva);
	}
	
	public Page<Pessoa> filtrar( PessoaFilter pessoaFilter, Pageable pageable ) {
		return pessoaRepository.filtrar(pessoaFilter, pageable);
	}
	
	private Pessoa buscarPeloCodigo(Long codigo) {
		
		Pessoa pessoa = pessoaRepository.findOne(codigo);
		
		if (pessoa == null) {
			throw new EmptyResultDataAccessException(1);
		}
		
		return pessoa;
	}
	
}