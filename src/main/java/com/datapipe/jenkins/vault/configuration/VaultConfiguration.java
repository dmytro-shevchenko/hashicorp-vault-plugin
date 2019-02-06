package com.datapipe.jenkins.vault.configuration;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.datapipe.jenkins.vault.credentials.VaultCredential;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;

public class VaultConfiguration extends AbstractDescribableImpl<VaultConfiguration> implements Serializable {
    private String vaultUrl;
    private String vaultCredentialId;
    private Boolean vaultRenew;
    private Integer vaultRenewHours;

    public VaultConfiguration() {
        // no args constructor
    }

    @DataBoundConstructor
    public VaultConfiguration(String vaultUrl, String vaultCredentialId, Boolean vaultRenew, Integer vaultRenewHours) {
        this.vaultUrl = normalizeUrl(vaultUrl);
        this.vaultCredentialId = vaultCredentialId;
        this.vaultRenew = vaultRenew;
        this.vaultRenewHours = vaultRenewHours;
    }

    public VaultConfiguration(VaultConfiguration toCopy) {
        this.vaultUrl = toCopy.getVaultUrl();
        this.vaultCredentialId = toCopy.getVaultCredentialId();
        this.vaultRenew = toCopy.getVaultRenew();
        this.vaultRenewHours = toCopy.getVaultRenewHours();
    }

    public VaultConfiguration mergeWithParent(VaultConfiguration parent) {
        if (parent == null) {
            return this;
        }
        VaultConfiguration result = new VaultConfiguration(this);
        if (StringUtils.isBlank(result.getVaultCredentialId())) {
            result.setVaultCredentialId(parent.getVaultCredentialId());
        }
        if (StringUtils.isBlank(result.getVaultUrl())) {
            result.setVaultUrl(parent.getVaultUrl());
        }
        //TODO: add renew merge here
        return result;
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultCredentialId() {
        return vaultCredentialId;
    }

    public Boolean getVaultRenew() {
        return vaultRenew;
    }

    public Integer getVaultRenewHours() {
        return vaultRenewHours;
    }

    @DataBoundSetter
    public void setVaultUrl(String vaultUrl) {
        this.vaultUrl = normalizeUrl(vaultUrl);
    }

    @DataBoundSetter
    public void setVaultCredentialId(String vaultCredentialId) {
        this.vaultCredentialId = vaultCredentialId;
    }

    @DataBoundSetter
    public void setVaultRenew(Boolean vaultRenew) {
        this.vaultRenew = vaultRenew;
    }

    @DataBoundSetter
    public void setVaultRenewHours(Integer vaultRenewHours) {
        this.vaultRenewHours = vaultRenewHours;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<VaultConfiguration> {
        @Override
        public String getDisplayName() {
            return "Vault Configuration";
        }

        public ListBoxModel doFillVaultCredentialIdItems(@AncestorInPath Item item, @QueryParameter String uri) {
            // This is needed for folders: credentials bound to a folder are
            // realized through domain requirements
            List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, item,
                    VaultCredential.class, domainRequirements);
        }
    }

    private String normalizeUrl(String url) {
        if(url == null) {
            return null;
        }

        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }

        return url;
    }
}
