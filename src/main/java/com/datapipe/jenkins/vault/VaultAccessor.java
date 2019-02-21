package com.datapipe.jenkins.vault;

import java.io.PrintStream;
import java.io.Serializable;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;
import com.bettercloud.vault.response.VaultResponse;
import com.datapipe.jenkins.vault.credentials.VaultCredential;
import com.datapipe.jenkins.vault.exception.VaultPluginException;

public class VaultAccessor implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Vault vault;

    private transient VaultConfig config;

    public void init(String url) {
        try {
            config = new VaultConfig().address(url).build();
            vault = new Vault(config);
        } catch (VaultException e) {
            throw new VaultPluginException("failed to connect to vault", e);
        }
    }

    public void auth(VaultCredential vaultCredential) {
        vault = vaultCredential.authorizeWithVault(vault, config);
    }

    public LogicalResponse read(String path) {
        try {
            return vault.logical().read(path);

        } catch (VaultException e) {
            throw new VaultPluginException("could not read from vault: " + e.getMessage() + " at path: " + path, e);
        }
    }

    public void tokenRenew(Integer hours, PrintStream logger) {
        try {
            AuthResponse response = vault.auth().renewSelf(hours*60*60);
            logger.println(String.format("Vault token ****%s updated with new lease: %s hours",
                    response.getAuthClientToken().substring(response.getAuthClientToken().length()-5), hours));
        } catch (Exception e) {
            System.out.println(String.format("ERROR: Failed to renew token! %s", e.getMessage()));
        }
    }

    public VaultResponse revoke(String leaseId) {
        try {
            return vault.leases().revoke(leaseId);
        } catch (VaultException e) {
            throw new VaultPluginException("could not revoke vault lease (" + leaseId + "):" + e.getMessage());
        }
    }
}
