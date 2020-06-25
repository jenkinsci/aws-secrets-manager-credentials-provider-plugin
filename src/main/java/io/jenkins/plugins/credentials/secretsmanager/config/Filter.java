package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.services.secretsmanager.model.FilterNameStringType;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

@Symbol("filter")
public class Filter extends AbstractDescribableImpl<Filter> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    private List<Value> values;

    @DataBoundConstructor
    public Filter(String key, List<Value> values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    public List<Value> getValues() {
        return values;
    }

    @DataBoundSetter
    public void setValues(List<Value> values) {
        this.values = values;
    }

    @Extension
    @Symbol("filter")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Filter> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.filter();
        }

        public FormValidation doCheckKey(@QueryParameter String key) {
            try {
                FilterNameStringType.fromValue(key);
            } catch (IllegalArgumentException e) {
                return FormValidation.error(e.getMessage());
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillKeyItems() {
            return allKeys();
        }

        private static ListBoxModel allKeys() {
            final ListBoxModel list = new ListBoxModel();

            for (FilterNameStringType key: FilterNameStringType.values()) {
                list.add(key.toString());
            }

            return list;
        }
    }
}
