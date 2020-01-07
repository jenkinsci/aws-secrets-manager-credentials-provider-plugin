package io.jenkins.plugins.credentials.secretsmanager.util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.rules.ExternalResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitHttpServer extends ExternalResource {
    private SimpleHttpServer server;
    private final List<String> repos;

    public GitHttpServer(List<String> repos) {
        this.repos = repos;
    }

    @Override
    protected void before() throws Throwable {
        final Path dataDir = Files.createTempDirectory("data");

        final String repoName = repos.stream().findFirst().orElseThrow(() -> new IllegalStateException("Git server must have a repo"));
        final Path repoDir = Files.createDirectories(dataDir.resolve("repos").resolve(repoName));
        final Repository repo = FileRepositoryBuilder.create(repoDir.resolve(".git").toFile());
        repo.create();

        try (Git git = new Git(repo)) {
            final Path readme = Files.createFile(repoDir.resolve("README"));
            git.add().addFilepattern(readme.getFileName().toString()).call();
            git.commit().setMessage("Initial commit").call();
        }

        server = new SimpleHttpServer(repo);
        server.start();
    }

    @Override
    protected void after() {
        if (this.server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                // Don't care
            }
        }
    }

    public String getCloneUrl(String repo, String username) {
        return server.getUri().toASCIIString();
        //return String.format("ssh://%s@localhost:%d/%s.git", username, port, repo);
    }
}
