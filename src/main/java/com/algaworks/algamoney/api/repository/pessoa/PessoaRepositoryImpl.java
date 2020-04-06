package com.algaworks.algamoney.api.repository.pessoa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.algaworks.algamoney.api.model.Pessoa;
import com.algaworks.algamoney.api.repository.filter.PessoaFilter;

public class PessoaRepositoryImpl implements PessoaRepositoryQuery {
	
	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public Page<Pessoa> filtrar(PessoaFilter filter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Pessoa> criteria = builder.createQuery(Pessoa.class);
		Root<Pessoa> root = criteria.from(Pessoa.class);
		
		Predicate[] predicates = criarRestricoes(filter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Pessoa> query = manager.createQuery(criteria);
		adicionarRestricoesParaPaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(filter));
	}
	
	private Predicate[] criarRestricoes(PessoaFilter filter, CriteriaBuilder builder, Root<Pessoa> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		if ( !StringUtils.isEmpty(filter.getNome()) ) {
			
			predicates.add( 
					builder.like( builder.lower(root.get("nome")), "%" + filter.getNome() + "%"));
			
		}
		
		if ( filter.getAtivo() != null ) {
			predicates.add( builder.equal(root.get("ativo"), filter.getAtivo()) );
		}
		
		return predicates.toArray( new Predicate[predicates.size()] );
	}
	
	private void adicionarRestricoesParaPaginacao(TypedQuery<Pessoa> query, Pageable pageable) {
		
		int paginaAtual = pageable.getPageNumber();
		int maximoElementosPorPagina = pageable.getPageSize();
		int primeiroElementoDaPagina = paginaAtual * maximoElementosPorPagina;
		
		query.setMaxResults(maximoElementosPorPagina);
		query.setFirstResult(primeiroElementoDaPagina);
		
	}
	
	private Long total(PessoaFilter filter) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Pessoa> root = criteria.from(Pessoa.class);
		
		Predicate[] predicates = criarRestricoes(filter, builder, root);
		criteria.where(predicates);
		
		criteria.select( builder.count(root) );
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
}