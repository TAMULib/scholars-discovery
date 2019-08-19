package edu.tamu.scholars.middleware.discovery.controller;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.tamu.scholars.middleware.discovery.assembler.DocumentResourceAssembler;
import edu.tamu.scholars.middleware.discovery.model.Document;
import edu.tamu.scholars.middleware.discovery.model.repo.DocumentRepo;
import edu.tamu.scholars.middleware.discovery.resource.DocumentResource;

@RepositoryRestController
@RequestMapping("/documents")
public class DocumentController extends AbstractSolrDocumentController<Document, DocumentRepo, DocumentResource, DocumentResourceAssembler> {

}
