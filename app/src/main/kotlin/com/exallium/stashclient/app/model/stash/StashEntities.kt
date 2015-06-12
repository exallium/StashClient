package com.exallium.stashclient.app.model.stash

import com.exallium.rxrecyclerview.lib.GroupComparator
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

public data class Group
public data class User
public data class Page<T>(var size: Int,
                          var limit: Int,
                          var isLastPage: Boolean,
                          var values: List<T>,
                          var start: Int,
                          var filter: String,
                          var nextPageStart: Int)
public data class SearchText(val text: String)
public data class Cluster
public data class License
public data class MailServer
public data class Permission<T>(val t: T, val permission: GlobalPermission)
public data class ApplicationProperties()
public data class LogInfo(val logLevel: LogLevel)
public data class MarkupPreview(val html: String)

public data class Repository(var slug: String,
                             var id: Int,
                             var name: String,
                             var scmId: String,
                             var state: String,
                             var statusMessage: String,
                             var forkable: String,
                             var project: Project,
                             var public: Boolean,
                             var cloneUrl: String,
                             var link: Link?): Comparable<Repository> {
    override fun compareTo(other: Repository): Int {
        return name.compareTo(other.name)
    }
}

public data class Project(var key: String,
                          var id: Int,
                          var name: String,
                          var description: String,
                          var public: Boolean,
                          var type: String,
                          var link: Link?): Comparable<Project> {
    override fun compareTo(other: Project): Int {
        return name.compareTo(other.name)
    }

}

public class StashFile(val name: String): Comparable<StashFile> {

    private val changeSubject: PublishSubject<StashFile> = PublishSubject.create()
    public val children: TreeMap<String, StashFile> = TreeMap()

    public fun isDirectory(): Boolean {
        return children.size() != 0
    }

    override fun compareTo(other: StashFile): Int {
        if (isDirectory() xor other.isDirectory()) {
            return if (isDirectory()) 1 else -1
        } else {
            return name.compareTo(other.name)
        }
    }

    public fun getOrCreate(path: String): StashFile {
        val pathTokens = path.split('/')
        val fileName = pathTokens[0]
        var child = children.get(fileName)
        if (child == null) {
            child = StashFile(fileName)
            children.put(fileName, child)
            changeSubject.onNext(child)
        }

        return if (pathTokens.size() > 1) {
            child.getOrCreate(pathTokens.drop(1).join("/"))
        } else {
            child
        }

    }

    public fun getObservable() : Observable<StashFile> {
        return changeSubject.mergeWith(Observable.from(children.map { it.getValue() }))
    }
}

public data class Link(var url: String, var rel: String)
public data class Permitted(val permitted: Boolean)
public data class Branch
public data class Change
public data class Commit
public data class Comment
public data class Diff
public data class PullRequest
public data class Activity
public data class Participant
public data class Task
public data class TaskCount(val open: Int, val resolved: Int)
public data class RepositoryHook
public data class Tag
public data class Credentials
public data class UserSettings

public enum class GlobalPermission {
    LICENCED_USER,
    PROJECT_CREATE,
    ADMIN,
    SYS_ADMIN
}

public enum class ProjectPermission {
    PROJECT_READ,
    PROJECT_WRITE,
    PROJECT_ADMIN
}

public enum class RepoPermission {
    REPO_READ,
    REPO_WRITE,
    REPO_ADMIN
}

public enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

public enum class LineType {
    ADDED,
    REMOVED,
    CONTEXT
}

public enum class FileType {
    FROM,
    TO
}
