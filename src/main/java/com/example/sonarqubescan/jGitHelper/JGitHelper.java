package com.example.sonarqubescan.jGitHelper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;


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

    public String getRepoPath(){
        return REPO_PATH;
    }


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

    public boolean checkout(String commit) {

        try {
            initCheckOut(commit);
            return true;
        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
            log.error("begin second checkout {}", this.REPO_PATH + commit);
            try {
                // clean for checkOut
                secondCheckOut(commit);
                return true;
            } catch (Exception e2) {
                log.error("second checkout error:{} ", e2.getMessage());
            }

        }
        return false;
    }

    private void initCheckOut(String commit) throws IOException, GitAPIException {
        // 不加上这一句  有新增和删除的情况还是会成功
        git.reset().setMode(ResetCommand.ResetType.HARD).call();

        if (commit == null) {
            commit = repository.getBranch();
        }
        CheckoutCommand checkoutCommand = git.checkout();
        checkoutCommand.setName(commit).call();
    }

    private void secondCheckOut(String commit) throws GitAPIException, IOException {
        // check index.lock
        File lock = new File(IS_WINDOWS ? REPO_PATH + "\\.git\\index.lock" : REPO_PATH + "/.git/index.lock");
        if (lock.exists() && lock.delete()) {
            log.error("repo[{}] index.lock exists, deleted! ", REPO_PATH);
        }


        git.reset().setMode(ResetCommand.ResetType.HARD).call();

        // check modify
        git.add().addFilepattern(".").call();
        git.stashCreate().call();

        initCheckOut(commit);
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

    public DiffFile getAllDiffFilePair(String commit) {
        List<String> addFiles = new ArrayList<>(8);
        List<String> deleteFiles = new ArrayList<>(8);
        Map<String, String> changeFiles = new HashMap<>(32);
        try{
            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = currCommit.getParents();

            for(RevCommit p : parentCommits){
                CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
                CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
                try (ObjectReader reader = repository.newObjectReader()) {
                    //get diff tree
                    oldTreeDiff.reset(reader, repository.resolve(p.getName() + "^{tree}"));
                    newTreeDiff.reset(reader, repository.resolve(commit + "^{tree}"));
                    //call git diff command
                    List<DiffEntry> diffs = getDiffEntry(getRevCommit(p.getName()), getRevCommit(commit), 60);
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
                    log.error("get diff file failed!pre commit is: {}, cur commit is: {}", p, commit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DiffFile(addFiles, deleteFiles, changeFiles);

    }
    public List<String> getFilesToScan(String commit){
        DiffFile diffFile = getAllDiffFilePair(commit);
        List<String> filesToScan = new ArrayList<>();
        filesToScan.addAll(diffFile.getAddFiles());
        filesToScan.addAll(diffFile.getChangeFiles().values());

        return filesToScan;
    }


//    public DiffFile getDiffFilePair(String preCommitId, String commitId) {
//
//        List<String> addFiles = new ArrayList<>(8);
//        List<String> deleteFiles = new ArrayList<>(8);
//        Map<String, String> changeFiles = new HashMap<>(32);
//
//
//        //init git diff
//        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
//        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
//        try (ObjectReader reader = repository.newObjectReader()) {
//            //get diff tree
//            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
//            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
//            //call git diff command
//            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
//            for (DiffEntry diff : diffs) {
//                switch (diff.getChangeType()) {
//                    case ADD:
//                        addFiles.add(diff.getNewPath());
//                        break;
//                    case DELETE:
//                        deleteFiles.add(diff.getOldPath());
//                        break;
//                    default:
//                        changeFiles.put(diff.getOldPath(), diff.getNewPath());
//                }
//            }
//        } catch (Exception e) {
//            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
//        }
//        return new DiffFile(addFiles, deleteFiles, changeFiles);
//    }
}
