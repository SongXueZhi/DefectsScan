package com.example.sonarqubescan.jGitHelper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 11:30 上午
 */
@Slf4j
public class JGitHelper {

    protected static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    protected Repository repository;

    protected RevWalk revWalk;

    protected Git git;

    protected String REPO_PATH;


    public JGitHelper(String repoPath) {
        REPO_PATH = repoPath;
        String gitDir = IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    @SneakyThrows
    public RevCommit getRevCommit(String commitId) {
        return revWalk.parseCommit(repository.resolve(commitId));
    }

    @SneakyThrows
    public List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit, int score) {
        // 不可少 否则parentCommit的 tree为null
        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(parentCommit.getTree());
        tw.addTree(currCommit.getTree());
        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        rd.setRenameScore(score);
        return rd.compute();
    }


    public DiffFile getDiffFilePair(String preCommitId, String commitId) {

        List<String> addFiles = new ArrayList<>(8);
        List<String> deleteFiles = new ArrayList<>(8);
        Map<String, String> changeFiles = new HashMap<>(32);


        //init git diff
        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            //get diff tree
            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
            //call git diff command
            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
            for (DiffEntry diff : diffs) {
                switch (diff.getChangeType()) {
                    case ADD:
                        addFiles.add(diff.getNewPath());
                        break;
                    case DELETE:
                        deleteFiles.add(diff.getOldPath());
                        break;
                    default:
                        changeFiles.put(diff.getOldPath(), diff.getNewPath());
                }
            }
        } catch (Exception e) {
            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
        }
        return new DiffFile(addFiles, deleteFiles, changeFiles);
    }
}
