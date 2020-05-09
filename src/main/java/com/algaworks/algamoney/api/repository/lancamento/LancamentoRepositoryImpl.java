package com.algaworks.algamoney.api.repository.lancamento;
	
import java.time.LocalDate;
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

import com.algaworks.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.algaworks.algamoney.api.dto.LancamentoEstatisticaDia;
import com.algaworks.algamoney.api.model.Lancamento;
import com.algaworks.algamoney.api.repository.filter.LancamentoFilter;
import com.algaworks.algamoney.api.repository.projection.ResumoLancamento;
	
public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {
	
	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		
		// utilizado para pegar os atributos da entidade que se deseja fazer o filtro
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		// criando restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesParaPaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}
	
	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select( 
				builder.construct(ResumoLancamento.class, 
				root.get("codigo"), root.get("descricao"),
				root.get("dataVencimento"), root.get("dataPagamento"),
				root.get("valor"), root.get("tipo"),
				root.get("categoria").get("nome"),
				root.get("pessoa").get("nome")) );
		
		// criando restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRestricoesParaPaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}
	
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaCategoria> criteria = builder.createQuery(LancamentoEstatisticaCategoria.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select( builder.construct(LancamentoEstatisticaCategoria.class,
				root.get("categoria"),
				builder.sum(root.get("valor")) ) );
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth( mesReferencia.lengthOfMonth() );
		
		criteria.where( 
				builder.greaterThanOrEqualTo( root.get("dataVencimento") , primeiroDia),
				builder.lessThanOrEqualTo( root.get("dataVencimento"), ultimoDia));
		
		criteria.groupBy( root.get("categoria") );
		
		TypedQuery<LancamentoEstatisticaCategoria> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaDia> criteria = builder.createQuery(LancamentoEstatisticaDia.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder.construct(LancamentoEstatisticaDia.class,
				root.get("tipo"),
				root.get("dataVencimento"),
				builder.sum( root.get("valor")) ));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth( mesReferencia.lengthOfMonth() );
		
		criteria.where(
				builder.greaterThanOrEqualTo(root.get("dataVencimento"), primeiroDia),
				builder.lessThanOrEqualTo(root.get("dataVencimento"), ultimoDia));
		
		criteria.groupBy( root.get("tipo"), root.get("dataVencimento") );
		
		TypedQuery<LancamentoEstatisticaDia> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder, Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add( builder.like(
					builder.lower(root.get("descricao")), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}
		
		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add( builder.greaterThanOrEqualTo( root.get("dataVencimento"), lancamentoFilter.getDataVencimentoDe() ));
		}
		
		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add( builder.lessThanOrEqualTo( root.get("dataVencimento") , lancamentoFilter.getDataVencimentoAte()) );
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private void adicionarRestricoesParaPaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistro = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistro);
		query.setMaxResults(totalRegistrosPorPagina);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select( builder.count(root) );
		
		return manager.createQuery(criteria).getSingleResult();
	}

}