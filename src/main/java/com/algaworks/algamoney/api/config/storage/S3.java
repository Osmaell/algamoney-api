package com.algaworks.algamoney.api.config.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.algaworks.algamoney.api.config.property.AlgamoneyApiProperty;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;

@Component
public class S3 {

	private static final Logger logger = LoggerFactory.getLogger(S3.class);

	@Autowired
	private AmazonS3 amazonS3;
	
	@Autowired
	private AlgamoneyApiProperty property;
	
	public void salvar(String objeto) {
		
		SetObjectTaggingRequest setObjectTaggingRequest = new SetObjectTaggingRequest(
				property.getS3().getBucket(), 
				objeto,
				new ObjectTagging(Collections.emptyList()));
		
		amazonS3.setObjectTagging(setObjectTaggingRequest);
		
	}
	
	public String salvarTemporariamente( MultipartFile arquivo ) {
		
		// Permissões
		AccessControlList acl = new AccessControlList();
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		
		// Metadados
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType( arquivo.getContentType() );
		objectMetadata.setContentLength( arquivo.getSize() );
		
		// Identificador único
		String nomeUnico = gerarNomeUnico( arquivo.getOriginalFilename() );
		
		try {
			
			// Criando um objeto de requisição para incluir o objeto (arquivo).
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					property.getS3().getBucket(),
					nomeUnico, 
					arquivo.getInputStream(), 
					objectMetadata)
					.withAccessControlList(acl);
			
			putObjectRequest.setTagging(new ObjectTagging( Arrays.asList( new Tag("expirar", "true") ) ));
			
			// fazendo o upload do objeto (arquivo) para o S3.
			amazonS3.putObject(putObjectRequest);
			
			if (logger.isErrorEnabled()) {
				logger.debug("Arquivo {} enviado com sucesso para o S3.", arquivo.getOriginalFilename());
			}
			
			return nomeUnico;
		} catch (IOException e) {
			throw new RuntimeException("Problemas ao enviar o arquivo para o S3.", e);
		}
		
	}
	
	public String configurarUrl(String objeto) {
		return "\\\\" + property.getS3().getBucket() + ".s3.amazonaws.com/" + objeto;
	}
	
	private String gerarNomeUnico(String originalFilename) {
		return UUID.randomUUID().toString() + "_" + originalFilename;
	}
	
}