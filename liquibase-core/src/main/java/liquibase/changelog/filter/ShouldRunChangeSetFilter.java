package liquibase.changelog.filter;

import java.util.List;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    private final List<RanChangeSet> ranChangeSets;
    private final Database database;
    private final boolean ignoringClasspathPrefix;

    public ShouldRunChangeSetFilter(Database database, boolean ignoringClasspathPrefix) throws DatabaseException {
        this.database = database;
        this.ignoringClasspathPrefix = ignoringClasspathPrefix;
        this.ranChangeSets = database.getRanChangeSetList();
    }

    public ShouldRunChangeSetFilter(Database database) throws DatabaseException {
        this(database, true);
    }
    
    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public boolean accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (changeSetsMatch(changeSet, ranChangeSet)) {
                if (changeSet.shouldAlwaysRun()) {
                    return true;
                }
                if (changeSet.shouldRunOnChange() && checksumChanged(changeSet, ranChangeSet)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    protected boolean changeSetsMatch(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return idsAreEqual(changeSet, ranChangeSet)
            && authorsAreEqual(changeSet, ranChangeSet)
            && pathsAreEqual(changeSet, ranChangeSet);
    }

    protected boolean idsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return ranChangeSet.getId().equals(changeSet.getId());
    }

    protected boolean authorsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return ranChangeSet.getAuthor().equals(changeSet.getAuthor());
    }

    private boolean pathsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return getPath(ranChangeSet).equalsIgnoreCase(getPath(changeSet));
    }

    protected boolean checksumChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum());
    }


    private String getPath(RanChangeSet ranChangeSet) {
        return stripClasspathPrefix(ranChangeSet.getChangeLog());
    }

    private String getPath(ChangeSet changeSet) {
        return stripClasspathPrefix(changeSet.getFilePath());
    }

    private String stripClasspathPrefix(String filePath) {
        if (ignoringClasspathPrefix) {
            return filePath.replaceFirst("^classpath:", "");
        }
        return filePath;
    }
}
