package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

    @Autowired
    UCSBOrganizationsRepository ucsbOrganizationsRepository;
    
    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganizations> allOrgs() {
        Iterable<UCSBOrganizations> orgs = ucsbOrganizationsRepository.findAll();
        return orgs;
    }

    @Operation(summary= "Create a new UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganizations postOrgs(
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrganizations org = new UCSBOrganizations();
        org.setOrgCode(orgCode);
        org.setOrgTranslationShort(orgTranslationShort);
        org.setOrgTranslation(orgTranslation);
        org.setInactive(inactive);

        UCSBOrganizations savedOrgs = ucsbOrganizationsRepository.save(org);

        return savedOrgs;
    }

    @Operation(summary= "Get a single UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganizations getById(
            @Parameter(name="code") @RequestParam String code) {
        UCSBOrganizations commons = ucsbOrganizationsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, code));

        return commons;
    }

    @Operation(summary= "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrgs(
            @Parameter(name="code") @RequestParam String code) {
        UCSBOrganizations orgs = ucsbOrganizationsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, code));

        ucsbOrganizationsRepository.delete(orgs);
        return genericMessage("UCSBOrganizations with id %s deleted".formatted(code));
    }

    @Operation(summary= "Update a single commons")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganizations updateOrgs(
            @Parameter(name="code") @RequestParam String code,
            @RequestBody @Valid UCSBOrganizations incoming) {

        UCSBOrganizations org = ucsbOrganizationsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, code));

        org.setOrgTranslationShort(incoming.getOrgTranslationShort());
        org.setOrgTranslation(incoming.getOrgTranslation());
        org.setInactive(incoming.getInactive());
        ucsbOrganizationsRepository.save(org);

        return org;
    }
}
