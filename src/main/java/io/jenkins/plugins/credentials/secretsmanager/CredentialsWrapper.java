package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithContextMenu;
import net.sf.json.JSONObject;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Collections;

import static com.cloudbees.plugins.credentials.CredentialsStoreAction.*;

public class CredentialsWrapper extends AbstractDescribableImpl<CredentialsWrapper>
            implements IconSpec, ModelObjectWithContextMenu, AccessControlled {

        private final DomainWrapper domain;

        /**
         * The {@link Credentials} that we are wrapping.
         */
        private final Credentials credentials;

        /**
         * The {@link StandardCredentials#getId()} of the {@link Credentials}.
         */
        private final String id;
        private Fingerprint fingerprint;

        public CredentialsWrapper(DomainWrapper domain, Credentials credentials, String id) {
            this.domain = domain;
            this.credentials = credentials;
            this.id = id;
        }

        /**
         * Return the id for the XML API.
         *
         * @return the id.
         * @since 2.1.0
         */
        @Exported
        public String getId() {
            return id;
        }

        public String getUrlName() {
            return Util.rawEncode(id);
        }

        @Override
        public String getIconClassName() {
            return credentials.getDescriptor().getIconClassName();
        }

        public Api getApi() {
            return new Api(this);
        }

        @Exported
        public String getDisplayName() {
            return CredentialsNameProvider.name(credentials);
        }

        /**
         * Gets the display name of the {@link CredentialsDescriptor}.
         *
         * @return the display name of the {@link CredentialsDescriptor}.
         */
        @Exported
        public String getTypeName() {
            return credentials.getDescriptor().getDisplayName();
        }

        @Exported
        public String getDescription() {
            return credentials instanceof StandardCredentials
                    ? ((StandardCredentials) credentials).getDescription()
                    : null;
        }

        @Exported
        public final String getFullName() {
            String n = getDomain().getFullName();
            if (n.length() == 0) {
                return getUrlName();
            } else {
                return n + '/' + getUrlName();
            }
        }

        /**
         * Gets the full display name of the {@link Credentials}.
         *
         * @return the full display name of the {@link Credentials}.
         */
        public final String getFullDisplayName() {
            String n = getDomain().getFullDisplayName();
            if (n.length() == 0) {
                return getDisplayName();
            } else {
                return n + " \u00BB " + getDisplayName();
            }
        }

        public Credentials getCredentials() {
            return credentials;
        }

        /**
         * Exposes the backing {@link DomainWrapper}.
         *
         * @return the backing {@link DomainWrapper}.
         */
        public DomainWrapper getDomain() {
            return domain;
        }

        /**
         * Exposes the backing {@link DomainWrapper}.
         *
         * @return the backing {@link DomainWrapper}.
         */
        public DomainWrapper getParent() {
            return domain;
        }

        public CredentialsStore getStore() {
            return domain.getStore();
        }

        /**
         * Exposes the fingerprint for Jelly pages.
         *
         * @return the {@link Fingerprint}.
         * @throws IOException if the {@link Fingerprint} could not be retrieved.
         * @since 2.1.1
         */
        @Restricted(NoExternalUse.class)
        @Exported(visibility = 1)
        public Fingerprint getFingerprint() throws IOException {
            if (fingerprint == null) {
                if (CredentialsProvider.FINGERPRINT_ENABLED) {
                    // idempotent write
                    fingerprint = CredentialsProvider.getFingerprintOf(credentials);
                }
            }
            return fingerprint;
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler web method
        public HttpResponse doDoDelete(StaplerRequest req) throws IOException {
            getStore().checkPermission(DELETE);
            if (getStore().removeCredentials(domain.getDomain(), credentials)) {
                return HttpResponses.redirectTo("../..");
            }
            return HttpResponses.redirectToDot();
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler web method
        public HttpResponse doDoMove(StaplerRequest req, @QueryParameter String destination) throws IOException {
            if (getStore().getDomains().size() <= 1) {
                return HttpResponses.status(400);
            }
            Jenkins jenkins = Jenkins.get();
            getStore().checkPermission(DELETE);
            final String splitKey = domain.getParent().getUrlName() + "/";
            int split = destination.lastIndexOf(splitKey);
            if (split == -1) {
                return HttpResponses.status(400);
            }
            String contextName = destination.substring(0, split);
            String domainName = destination.substring(split + splitKey.length());
            ModelObject context = null;
            if ("".equals(contextName)) {
                context = jenkins;
            } else {
                while (context == null && split > 0) {
                    context = contextName.startsWith("user:")
                            ? User
                            .get(contextName.substring("user:".length(), split - 1), false, Collections.emptyMap())
                            : jenkins.getItemByFullName(contextName);
                    if (context == null) {
                        split = destination.lastIndexOf(splitKey, split - 1);
                        if (split > 0) {
                            contextName = destination.substring(0, split);
                            domainName = destination.substring(split + splitKey.length());
                        }
                    }
                }
            }
            if (context == null) {
                return HttpResponses.status(400);
            }
            CredentialsStore destinationStore = null;
            Domain destinationDomain = null;
            for (CredentialsStore store : CredentialsProvider.lookupStores(context)) {
                if (store.getContext() == context) {
                    for (Domain d : store.getDomains()) {
                        if (domainName.equals("_") ? d.getName() == null : domainName.equals(d.getName())) {
                            destinationStore = store;
                            destinationDomain = d;
                            break;
                        }
                    }
                    if (destinationDomain != null) {
                        break;
                    }
                }
            }
            if (destinationDomain == null) {
                return HttpResponses.status(400);
            }
            if (!destinationStore.isDomainsModifiable()) {
                return HttpResponses.status(400);
            }
            destinationStore.checkPermission(CREATE);
            if (destinationDomain.equals(domain.getDomain())) {
                return HttpResponses.redirectToDot();
            }

            if (destinationStore.addCredentials(destinationDomain, credentials)) {
                if (getStore().removeCredentials(domain.getDomain(), credentials)) {
                    return HttpResponses.redirectTo("../..");
                } else {
                    destinationStore.removeCredentials(destinationDomain, credentials);
                }
            }
            return HttpResponses.redirectToDot();
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler web method
        public HttpResponse doUpdateSubmit(StaplerRequest req) throws ServletException, IOException {
            getStore().checkPermission(UPDATE);
            JSONObject data = req.getSubmittedForm();
            Credentials credentials = req.bindJSON(Credentials.class, data);
            if (!getStore().updateCredentials(this.domain.domain, this.credentials, credentials)) {
                return HttpResponses.redirectTo("concurrentModification");
            }
            return HttpResponses.redirectToDot();
        }

        @CheckForNull
        @Restricted(NoExternalUse.class)
        public ContextMenu getContextMenu(String prefix) {
            if (getStore().hasPermission(UPDATE) || getStore().hasPermission(DELETE)) {
                ContextMenu result = new ContextMenu();
                if (getStore().hasPermission(UPDATE)) {
                    result.add(new MenuItem(
                            ContextMenuIconUtils.buildUrl(prefix, "update"),
                            getMenuItemIconUrlByClassSpec("icon-setting icon-md"),
                            Messages.CredentialsStoreAction_UpdateCredentialAction()
                    ));
                }
                if (getStore().hasPermission(DELETE)) {
                    result.add(new MenuItem(ContextMenuIconUtils.buildUrl(prefix, "delete"),
                            getMenuItemIconUrlByClassSpec("icon-edit-delete icon-md"),
                            Messages.CredentialsStoreAction_DeleteCredentialAction()
                    ));
                    result.add(new MenuItem(ContextMenuIconUtils.buildUrl(prefix, "move"),
                            getMenuItemIconUrlByClassSpec("icon-credentials-move icon-md"),
                            Messages.CredentialsStoreAction_MoveCredentialAction()
                    ));
                }
                return result.items.isEmpty() ? null : result;
            }
            return null;
        }

        @Override
        public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) {
            return getContextMenu("");
        }

        /**
         * Accepts {@literal config.xml} submission, as well as serve it.
         *
         * @param req the request
         * @param rsp the response
         * @throws IOException if things go wrong
         * @since 2.1.1
         */
        @WebMethod(name = "config.xml")
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler web method
        public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp)
                throws IOException {
            if (req.getMethod().equals("GET")) {
                // read
                getStore().checkPermission(VIEW);
                rsp.setContentType("application/xml");
                SECRETS_REDACTED.toXML(credentials,
                        new OutputStreamWriter(rsp.getOutputStream(), rsp.getCharacterEncoding()));
                return;
            }
            if (req.getMethod().equals("POST")) {
                // submission
                updateByXml(new StreamSource(req.getReader()));
                return;
            }
            if (req.getMethod().equals("DELETE")) {
                getStore().checkPermission(DELETE);
                if (getStore().removeCredentials(domain.getDomain(), credentials)) {
                    return;
                } else {
                    rsp.sendError(HttpServletResponse.SC_CONFLICT);
                    return;
                }
            }

            // huh?
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

        /**
         * Updates a {@link Credentials} by its XML definition.
         *
         * @param source source of the Item's new definition.
         *               The source should be either a <code>StreamSource</code> or a <code>SAXSource</code>, other
         *               sources may not be handled.
         * @throws IOException if things go wrong
         * @since 2.1.1
         */
        @Restricted(NoExternalUse.class)
        public void updateByXml(Source source) throws IOException {
            getStore().checkPermission(UPDATE);
            final StringWriter out = new StringWriter();
            try {
                XMLUtils.safeTransform(source, new StreamResult(out));
                out.close();
            } catch (TransformerException | SAXException e) {
                throw new IOException("Failed to parse credential", e);
            }

            Credentials credentials = (Credentials)
                    Items.XSTREAM.unmarshal(new XppDriver().createReader(new StringReader(out.toString())));
            getStore().updateCredentials(domain.getDomain(), this.credentials, credentials);
        }

        @NonNull
        @Override
        public ACL getACL() {
            return getParent().getACL();
        }

        @Override
        public void checkPermission(@NonNull Permission permission) throws AccessDeniedException {
            getACL().checkPermission(permission);
        }

        @Override
        public boolean hasPermission(@NonNull Permission permission) {
            return getACL().hasPermission(permission);
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<CredentialsWrapper> {

            /**
             * Exposes {@link CredentialsProvider#allCredentialsDescriptors()} to Jelly
             *
             * @return {@link CredentialsProvider#allCredentialsDescriptors()}
             */
            @Restricted(NoExternalUse.class)
            public DescriptorExtensionList<Credentials, CredentialsDescriptor> getCredentialDescriptors() {
                // TODO delete me
                return CredentialsProvider.allCredentialsDescriptors();
            }

            @NonNull
            @Override
            public String getDisplayName() {
                return "Credential";
            }
        }
    }