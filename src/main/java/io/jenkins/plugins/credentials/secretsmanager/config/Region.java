package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class Region extends AbstractDescribableImpl<Region> implements Serializable {

    private String region;

    @DataBoundConstructor
    public Region(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    @DataBoundSetter
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region1 = (Region) o;
        return Objects.equals(region, region1.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region);
    }

    @Extension
    @Symbol("region")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Region> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.region();
        }
    }
}
