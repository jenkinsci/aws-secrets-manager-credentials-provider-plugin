/*
 * The MIT License
 *
 * Copyright 2018 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.aws_secrets_manager_credentials_provider;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;

import org.acegisecurity.Authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import jenkins.model.Jenkins;

@Extension
public class AWSCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AWSCredentialsProvider.class.getName());

    private final AWSCredentialsStore store = new AWSCredentialsStore(this);

    private final Supplier<Collection<IdCredentials>> credentialsSupplier = CredentialsSupplierFactory.create();

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(Class<C> type, ItemGroup itemGroup, Authentication authentication) {
        if (ACL.SYSTEM.equals(authentication)) {
            final ArrayList<C> list = new ArrayList<>();
            for (IdCredentials credential : credentialsSupplier.get()) {
                // is s a type of type then populate the list...
                if (type.isAssignableFrom(credential.getClass())) {
                    // cast to keep generics happy even though we are assignable..
                    list.add(type.cast(credential));
                }
                LOG.log(Level.FINEST, "getCredentials {0} does not match", credential.getId());
            }
            return list;
        }

        return Collections.emptyList();
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        return object == Jenkins.getInstance() ? store : null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }
}
