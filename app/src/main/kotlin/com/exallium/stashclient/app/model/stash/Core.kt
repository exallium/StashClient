package com.exallium.stashclient.app.model.stash

import android.net.Uri
import retrofit.http.*
import retrofit.mime.TypedFile
import rx.Observable

private val corePath = "/rest/api/1.0"
private val adminPath = corePath + "/admin"
private val groupsAdminPath = adminPath + "/groups"
private val usersAdminPath = adminPath + "/users"
private val clusterAdminPath = adminPath + "/cluster"
private val licenseAdminPath = adminPath + "/license"
private val mailServerAdminPath = adminPath + "/mail-server"
private val mailServerSenderAddressAdminPath = mailServerAdminPath + "/sender-address"
private val permissionsAdminPath = adminPath + "/permissions"
private val groupPermissionsAdminPath = permissionsAdminPath + "/groups"
private val userPermissionsAdminPath = permissionsAdminPath + "/users"
private val logsPath = corePath + "/logs"
private val rootLoggerPath = logsPath + "/rootLogger"
private val profilePath = corePath + "/profile"
private val profileRecentPath = profilePath + "/recent"
private val projectsPath = corePath + "/projects"
private val projectPath = projectsPath + "/{projectKey}"
private val projectPermissionsPath = projectPath + "/permissions"
private val projectRepos = projectPath + "/repos"
private val projectRepo = projectRepos + "/{repositorySlug}"
private val projectRepoPermissions = projectRepo + "/permissions"
private val projectRepoPullRequests = projectRepo + "/pull-requests"
private val projectRepoPullRequest = projectRepoPullRequests + "/{pullRequestId}"
private val projectRepoHooks = projectRepo + "/settings/hooks"
private val projectRepoHook = projectRepoHooks + "/{hookKey}"

public interface Core {

    public interface System {
        @GET(corePath + "/application-properties")
        fun appInfo(): Observable<ApplicationProperties>
    }

    public interface Admin {
        public interface Groups {
            @GET(groupsAdminPath)
            fun retrieve(@Query("filter") groupFilter: String = ""): Observable<Page<Group>>

            @POST(groupsAdminPath)
            fun create(@Query("name") groupName: String): Observable<Group>

            @DELETE(groupsAdminPath)
            fun delete(@Query("name") groupName: String)

            @POST(groupsAdminPath + "/add-users")
            fun addUsers(@Field("group") groupName: String,
                         @Field("users") userList: List<User>)

            @GET(groupsAdminPath + "/more-members")
            fun retrieveMembers(@Query("context") groupName: String,
                                @Query("filter") userFilter: String = ""): Observable<Page<User>>

            @GET(groupsAdminPath + "/more-non-members")
            fun retrieveNonMembers(@Query("context") groupName: String,
                                   @Query("filter") userFilter: String = ""): Observable<Page<User>>

        }

        interface Users {
            @POST(usersAdminPath + "/add-groups")
            fun addGroups(@Field("user") userName: String,
                          @Field("groups") groupList: List<Group>)

            @POST(usersAdminPath + "/remove-group")
            fun removeGroup(@Field("context") userName: String,
                            @Field("itemName") groupName: String)

            @PUT(usersAdminPath + "/credentials")
            fun updatePassword(@Field("password") password: String,
                               @Field("passwordConfirm") passwordConfirm: String,
                               @Field("name") userName: String)
        }

        interface Clusters {
            @GET(clusterAdminPath)
            fun retrieve(): Observable<Cluster>
        }

        interface Licenses {
            @GET(licenseAdminPath)
            fun retrieve(): Observable<License>

            @POST(licenseAdminPath)
            fun update(@Field("license") encodedLicense: String): Observable<License>
        }

        interface MailServer {
            @DELETE(mailServerAdminPath)
            fun delete()

            @GET(mailServerAdminPath)
            fun retrieve(): Observable<MailServer>

            @PUT(mailServerAdminPath)
            fun update(@Body mailServer: MailServer): Observable<MailServer>

            interface SenderAddress {
                @DELETE(mailServerSenderAddressAdminPath)
                fun delete()

                @GET(mailServerSenderAddressAdminPath)
                fun retrieve(): Observable<String>

                @PUT(mailServerSenderAddressAdminPath)
                fun update(@Body address: String): Observable<String>
            }

        }

        interface Permissions {
            interface Groups {
                @GET(groupPermissionsAdminPath)
                fun retrieve(@Query("filter") groupFilter: String = ""): Observable<Page<Permission<User>>>

                @PUT(groupPermissionsAdminPath)
                fun update(@Field("permission") globalPermission: GlobalPermission,
                           @Field("name") groupNames: List<String>)

                @DELETE(groupPermissionsAdminPath)
                fun delete()

                @GET(groupPermissionsAdminPath + "/none")
                fun none(@Query("filter") groupFilter: String = ""): Observable<Page<Group>>

            }

            interface Users {
                @GET(userPermissionsAdminPath)
                fun retrieve(@Query("filter") userFilter: String = ""): Observable<Page<Permission<User>>>

                @PUT(userPermissionsAdminPath)
                fun update(@Field("permission") globalPermission: GlobalPermission,
                           @Field("name") userNames: List<String>)

                @DELETE(userPermissionsAdminPath)
                fun delete()

                @GET(userPermissionsAdminPath + "/none")
                fun none(@Query("filter") userFilter: String = ""): Observable<Page<User>>
            }
        }
    }

    public interface Groups {
        @GET(corePath + "/groups")
        fun retrieve(@Query("filter") groupFilter: String = ""): Observable<Page<Group>>
    }

    public object Hooks {
        fun getUri(moduleKey: String, version: String = ""): Uri {
            return Uri.parse(corePath + "/hooks/" + moduleKey + "/avatar"
                    + (if ("".equals(version)) "" else "?version=" + version))
        }
    }

    public interface Logs {

        @PUT(rootLoggerPath + "/{levelName}")
        fun update(@Path("levelName") levelName: LogLevel)

        @GET(rootLoggerPath)
        fun retrieve(): Observable<LogInfo>

        @PUT(logsPath + "/{loggerName}/{levelName}")
        fun update(@Path("loggerName") loggerName: String, @Path("levelName") levelName: LogLevel)

        @GET(logsPath + "/{loggerName}")
        fun retrieve(@Path("loggerName") loggerName: String): Observable<LogInfo>
    }

    public interface Markup {
        @POST(corePath + "/markup/preview")
        fun preview(@Body markup: String,
                    @Query("urlMode") urlMode: String,
                    @Query("hardwrap") hardwrap: Boolean,
                    @Query("htmlEscape") htmlEscape: Boolean)
    }

    public interface Profile {
        public interface Repos {
            @GET(profileRecentPath + "/repos")
            fun retrieveRecent(@Query("permission") repoPermission: RepoPermission = RepoPermission.REPO_READ): Observable<Page<Repository>>
        }
    }

    public interface Projects {
        @POST(projectsPath)
        fun create(@Body project: Project): Observable<Project>

        @GET(projectsPath)
        fun retrieve(@Query("name") projectName: String = "",
                     @Query("permission") projectPermission: ProjectPermission = ProjectPermission.PROJECT_READ,
                     @Query("start") start: Int = 0): Observable<Page<Project>>

        @PUT(projectPath)
        fun update(@Path("projectKey") projectKey: String, @Body project: Project): Observable<Project>

        @DELETE(projectPath)
        fun delete(@Path("projectKey") projectKey: String)

        @GET(projectPath)
        fun retrieve(@Path("projectKey") projectKey: String): Observable<Project>

        @Multipart
        @POST(projectPath + "/avatar")
        fun update(@Path("projectKey") projectKey: String, @Part("avatar") image: TypedFile)

        public object Avatar {
            public fun getUri(projectKey: String, s: Int = 0): Uri {
                return Uri.parse(projectsPath + projectKey + "/avatar?s=" + s.toString())
            }
        }

        public interface Permissions {

            @GET(projectPermissionsPath + "/{permission}/all")
            fun retrieve(@Path("projectKey") projectKey: String,
                         @Path("permission") projectPermission: ProjectPermission): Observable<Permitted>

            @POST(projectPermissionsPath + "/{permission}/all")
            fun update(@Path("projectKey") projectKey: String,
                       @Path("permission") projectPermission: ProjectPermission): Observable<Permitted>


            public interface Groups {
                @GET(projectPermissionsPath + "/groups")
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Query("filter") groupFilter: String = ""): Observable<Page<Group>>

                @PUT(projectPermissionsPath + "/groups")
                fun update(@Path("projectKey") projectKey: String,
                           @Query("permission") projectPermission: ProjectPermission,
                           @Query("name") groupName: String)

                @DELETE(projectPermissionsPath + "/groups")
                fun delete(@Path("projectKey") projectKey: String,
                           @Query("name") groupName: String)

                @GET(projectPermissionsPath + "/groups/none")
                fun none(@Path("projectKey") projectKey: String,
                         @Query("filter") groupFilter: String = ""): Observable<Page<Group>>

            }

            public interface Users {
                @GET(projectPermissionsPath + "/users")
                fun retrieve(@Path("projectKey") projectKey: String,
                                    @Query("filter") userFilter: String = ""): Observable<Page<User>>

                @PUT(projectPermissionsPath + "/users")
                fun update(@Path("projectKey") projectKey: String,
                           @Query("permission") projectPermission: ProjectPermission,
                           @Query("name") userName: String)

                @DELETE(projectPermissionsPath + "/users")
                fun delete(@Path("projectKey") projectKey: String,
                           @Query("name") userName: String)

                @GET(projectPermissionsPath + "/users/none")
                fun none(@Path("projectKey") projectKey: String,
                         @Query("filter") userFilter: String = ""): Observable<Page<User>>
            }
        }

        public interface Repos {
            @GET(projectRepos)
            fun retrieve(@Path("projectKey") projectKey: String): Observable<Page<Repository>>

            @POST(projectRepos)
            fun create(@Path("projectKey") projectKey: String, @Body repository: Repository): Observable<Repository>

            @GET(projectRepo)
            fun retrieve(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String): Observable<Repository>

            @DELETE(projectRepo)
            fun delete(@Path("projectKey") projectKey: String,
                       @Path("repositorySlug") repositorySlug: String): Observable<Repository>

            @PUT(projectRepo)
            fun update(@Path("projectKey") projectKey: String,
                       @Path("repositorySlug") repositorySlug: String,
                       @Body repository: Repository): Observable<Repository>

            @GET(projectRepo + "/forks")
            fun forks(@Path("projectKey") projectKey: String,
                      @Path("repositorySlug") repositorySlug: String): Observable<Page<Repository>>

            @POST(projectRepo + "/recreate")
            fun recreate(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String): Observable<Repository>

            @GET(projectRepo + "/related")
            fun related(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String): Observable<Repository>

            @GET(projectRepo + "/branches")
            fun branches(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String,
                         @Query("base") base: String,
                         @Query("details") details: Boolean,
                         @Query("filterText") filterText: String,
                         @Query("orderBy") orderBy: String): Observable<Page<Branch>>

            @GET(projectRepo + "/branches/default")
            fun retrieveDefaultBranch(@Path("projectKey") projectKey: String,
                              @Path("repositorySlug") repositorySlug: String): Observable<Branch>

            @PUT(projectRepo + "/branches/default")
            fun updateDefaultBranch(@Path("projectKey") projectKey: String,
                                    @Path("repositorySlug") repositorySlug: String): Observable<Branch>

            @GET(projectRepo + "/browse/{path}")
            fun browse(@Path("projectKey") projectKey: String,
                       @Path("repositorySlug") repositorySlug: String,
                       @Path("path") path: String = "",
                       @Query("at") at: String = "",
                       @Query("type") type: Boolean = false): Observable<Page<SearchText>>

            @GET(projectRepo + "/browse/{path}")
            fun browse(@Path("projectKey") projectKey: String,
                       @Path("repositorySlug") repositorySlug: String,
                       @Path("path") path: String = "",
                       @Query("at") at: String = "",
                       @Query("type") type: Boolean = false,
                       @Query("blame") blame: String): Observable<Page<SearchText>>

            @GET(projectRepo + "/browse/{path}")
            fun browse(@Path("projectKey") projectKey: String,
                       @Path("repositorySlug") repositorySlug: String,
                       @Path("path") path: String = "",
                       @Query("at") at: String = "",
                       @Query("type") type: Boolean = false,
                       @Query("blame") blame: String,
                       @Query("noContent") noContent: String): Observable<Page<SearchText>>

            @GET(projectRepo + "/changes")
            fun changes(@Path("projectKey") projectKey: String,
                        @Path("repositorySlug") repositorySlug: String,
                        @Query("since") since: String,
                        @Query("until") until: String): Observable<Page<Change>>

            @GET(projectRepo + "/compare/changes")
            fun compareChanges(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Query("from") from: String,
                               @Query("to") to: String,
                               @Query("fromRepo") fromRepo: String): Observable<Page<Change>>

            @GET(projectRepo + "/compare/commits")
            fun compareCommits(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Query("from") from: String,
                               @Query("to") to: String,
                               @Query("fromRepo") fromRepo: String): Observable<Page<Commit>>

            @GET(projectRepo + "/compare/diff{path}")
            fun compareDiffs(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Path("path") path: String = "",
                             @Query("from") from: String,
                             @Query("to") to: String,
                             @Query("fromRepo") fromRepo: String,
                             @Query("srcPath") srcPath: String,
                             @Query("contextLines") contextLines: Int = -1,
                             @Query("withComments") withComments: Boolean = true): Observable<Page<Diff>>

            @GET(projectRepo + "/diff/{path}")
            fun diff(@Path("projectKey") projectKey: String,
                     @Path("repositorySlug") repositorySlug: String,
                     @Path("path") path: String = "",
                     @Query("contextLines") contextLines: Int = -1,
                     @Query("since") since: String,
                     @Query("srcPath") srcPath: String,
                     @Query("until") until: String,
                     @Query("whitespace") whitespace: String = ""): Observable<Diff>

            @GET(projectRepo + "/files/{path}")
            fun files(@Path("projectKey") projectKey: String,
                      @Path("repositorySlug") repositorySlug: String,
                      @Path("path") path: String = "",
                      @Query("at") at: String = ""): Observable<Page<String>>

            public enum class TagOrder {
                ALPHABETICAL,
                MODIFICATION
            }

            @GET(projectRepo + "/tags")
            fun tags(@Path("projectKey") projectKey: String,
                     @Path("repositorySlug") repositorySlug: String,
                     @Query("filterText") filterText: String,
                     @Query("orderBy") orderBy: TagOrder = TagOrder.MODIFICATION): Observable<Page<Tag>>

            public interface Commits {
                @GET(projectRepo + "/commits/{commitId}")
                fun all(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Query("path") path: String,
                            @Query("since") since: String,
                            @Query("until") until: String,
                            @Query("withCounts") withCounts: Boolean): Observable<Page<Commit>>

                @GET(projectRepo + "/commits/{commitId}")
                fun single(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Path("commitId") commitId: String,
                           @Query("path") path: String): Observable<Commit>

                @GET(projectRepo + "commits/{commitId}/changes")
                fun changes(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("commitId") commitId: String,
                            @Query("since") since: String,
                            @Query("withComments") withComments: Boolean): Observable<Page<Change>>

                @GET(projectRepo + "commits/{commitId}/{path}")
                fun diff(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String,
                         @Path("commitId") commitId: String,
                         @Path("path") path: String = "",
                         @Query("contextLines") contextLines: Int = -1,
                         @Query("since") since: String,
                         @Query("srcPath") srcPath: String,
                         @Query("whitespace") whitespace: String = "",
                         @Query("withComments") withComments: Boolean = true): Observable<Page<Diff>>

                @DELETE(projectRepo + "commits/{commitId}/watch")
                fun unwatch(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("commitId") commitId: String)

                @POST(projectRepo + "commits/{commitId}/watch")
                fun watch(@Path("projectKey") projectKey: String,
                          @Path("repositorySlug") repositorySlug: String,
                          @Path("commitId") commitId: String)

                public interface Comments {
                    @POST(projectRepo + "commits/{commitId}/comments")
                    fun create(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("commitId") commitId: String,
                               @Body comment: Comment,
                               @Query("since") since: String)

                    @GET(projectRepo + "commits/{commitId}/comments")
                    fun retrieve(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Path("commitId") commitId: String,
                                 @Query("path") path: String,
                                 @Query("since") since: String): Observable<Page<Comment>>

                    @GET(projectRepo + "commits/{commentId}/comments/{commentId}")
                    fun retrieve(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Path("commitId") commitId: String,
                                 @Path("commentId") commentId: Long): Observable<Page<Comment>>

                    @PUT(projectRepo + "commits/{commentId}/comments/{commentId}")
                    fun update(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("commitId") commitId: String,
                               @Path("commentId") commentId: Long,
                               @Body comment: Comment)

                    @DELETE(projectRepo + "commits/{commentId}/comments/{commentId}")
                    fun delete(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("commitId") commitId: String,
                               @Path("commentId") commentId: Long,
                               @Query("version") version: Int = -1)
                }

                public interface Permissions {
                    public interface Groups {
                        @GET(projectRepoPermissions + "/groups")
                        fun retrieve(@Path("projectKey") projectKey: String,
                                     @Path("repositorySlug") repositorySlug: String,
                                     @Query("filter") groupFilter: String = ""): Observable<Page<Group>>

                        @PUT(projectRepoPermissions + "/groups")
                        fun update(@Path("projectKey") projectKey: String,
                                   @Path("repositorySlug") repositorySlug: String,
                                   @Query("permission") projectPermission: ProjectPermission,
                                   @Query("name") groupName: String)

                        @DELETE(projectRepoPermissions + "/groups")
                        fun delete(@Path("projectKey") projectKey: String,
                                   @Path("repositorySlug") repositorySlug: String,
                                   @Query("name") groupName: String)

                        @GET(projectRepoPermissions + "/groups/none")
                        fun none(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Query("filter") groupFilter: String = ""): Observable<Page<Group>>

                    }

                    public interface Users {
                        @GET(projectRepoPermissions + "/users")
                        fun retrieve(@Path("projectKey") projectKey: String,
                                     @Path("repositorySlug") repositorySlug: String,
                                     @Query("filter") userFilter: String = ""): Observable<Page<User>>

                        @PUT(projectRepoPermissions + "/users")
                        fun update(@Path("projectKey") projectKey: String,
                                   @Path("repositorySlug") repositorySlug: String,
                                   @Query("permission") projectPermission: ProjectPermission,
                                   @Query("name") userName: String)

                        @DELETE(projectRepoPermissions + "/users")
                        fun delete(@Path("projectKey") projectKey: String,
                                   @Path("repositorySlug") repositorySlug: String,
                                   @Query("name") userName: String)

                        @GET(projectRepoPermissions + "/users/none")
                        fun none(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Query("filter") userFilter: String = ""): Observable<Page<User>>
                    }
                }
            }

            public interface PullRequests {

                public enum class Order {
                    OLDEST,
                    NEWEST
                }

                public enum class State {
                    ANY,
                    OPEN,
                    DECLINED,
                    MERGED
                }

                public enum class Direction {
                    INCOMING,
                    OUTGOING
                }

                public enum class ActivityType {
                    ACTIVITY,
                    COMMENT
                }

                @POST(projectRepoPullRequests)
                fun create(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Body pullRequest: PullRequest)

                @GET(projectRepoPullRequests)
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Query("direction") direction: Direction,
                             @Query("at") at: String,
                             @Query("state") state: State,
                             @Query("order") order: Order,
                             @Query("withAttributes") withAttributes: Boolean = true,
                             @Query("withProperties") withProperties: Boolean = true):  Observable<Page<PullRequest>>

                @PUT(projectRepoPullRequest)
                fun update(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Path("pullRequestId") pullRequestId: Long,
                           @Body pullRequest: PullRequest): Observable<PullRequest>

                @GET(projectRepoPullRequest)
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Path("pullRequestId") pullRequestId: Long): Observable<PullRequest>

                @GET(projectRepoPullRequest + "/activity")
                fun activities(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long,
                               @Query("fromId") fromId: Long,
                               @Query("fromType") fromType: ActivityType): Observable<Page<Activity>>

                @POST(projectRepoPullRequest + "/decline")
                fun decline(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("pullRequestId") pullRequestId: Long,
                            @Query("version") version: Int = -1)

                @GET(projectRepoPullRequest + "/merge")
                fun canMerge(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Path("pullRequestId") pullRequestId: Long): Observable<PullRequest>

                @POST(projectRepoPullRequest + "/merge")
                fun merge(@Path("projectKey") projectKey: String,
                          @Path("repositorySlug") repositorySlug: String,
                          @Path("pullRequestId") pullRequestId: Long,
                          @Query("version") version: Int = -1): Observable<PullRequest>

                @POST(projectRepoPullRequest + "/reopen")
                fun reopen(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Path("pullRequestId") pullRequestId: Long,
                           @Query("version") version: Int = -1): Observable<PullRequest>

                @POST(projectRepoPullRequest + "/approve")
                fun approve(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("pullRequestId") pullRequestId: Long)

                @DELETE(projectRepoPullRequest + "/approve")
                fun unapprove(@Path("projectKey") projectKey: String,
                              @Path("repositorySlug") repositorySlug: String,
                              @Path("pullRequestId") pullRequestId: Long)

                @GET(projectRepoPullRequest + "/changes")
                fun changes(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("pullRequestId") pullRequestId: Long,
                            @Query("withComments") withComments: Boolean = true): Observable<Page<Change>>

                @GET(projectRepoPullRequest + "/commits")
                fun commits(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("pullRequestId") pullRequestId: Long,
                            @Query("withCounts") withComments: Boolean = false): Observable<Page<Commit>>

                @GET(projectRepoPullRequest + "/diff/{path}")
                fun diff(@Path("projectKey") projectKey: String,
                         @Path("repositorySlug") repositorySlug: String,
                         @Path("pullRequestId") pullRequestId: Long,
                         @Path("path") path: String,
                         @Query("contextLines") contextLines: Int = -1,
                         @Query("srcPath") srcPath: String,
                         @Query("whitespace") whitespace: String = "",
                         @Query("withComments") withComments: Boolean = true): Observable<Page<Diff>>

                @GET(projectRepoPullRequest + "/tasks")
                fun tasks(@Path("projectKey") projectKey: String,
                          @Path("repositorySlug") repositorySlug: String,
                          @Path("pullRequestId") pullRequestId: Long): Observable<Page<Task>>

                @DELETE(projectRepoPullRequest + "/watch")
                fun unwatch(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("pullRequestId") pullRequestId: Long)

                @POST(projectRepoPullRequest + "/watch")
                fun watch(@Path("projectKey") projectKey: String,
                          @Path("repositorySlug") repositorySlug: String,
                          @Path("pullRequestId") pullRequestId: Long)

                public interface Participants {
                    @POST(projectRepoPullRequest + "/participants")
                    fun assign(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long): Observable<Participant>

                    @DELETE(projectRepoPullRequest + "/participants")
                    fun remove(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long)

                    @GET(projectRepoPullRequest + "/participants")
                    fun retrieve(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Path("pullRequestId") pullRequestId: Long): Observable<Page<Participant>>
                }

                public interface Comments {
                    @GET(projectRepoPullRequest + "/comments")
                    fun retrieve(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Path("pullRequestId") pullRequestId: Long,
                                 @Query("path") path: String = ""): Observable<Page<Comment>>

                    @POST(projectRepoPullRequest + "/comments")
                    fun create(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long,
                               @Body comment: Comment): Comment

                    @DELETE(projectRepoPullRequest + "/comments/{commentId}")
                    fun delete(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long,
                               @Path("commentId") commentId: Long)

                    @PUT(projectRepoPullRequest + "/comments/{commentId}")
                    fun update(@Path("projectKey") projectKey: String,
                               @Path("repositorySlug") repositorySlug: String,
                               @Path("pullRequestId") pullRequestId: Long,
                               @Path("commentId") commentId: Long): Observable<Comment>

                    @GET(projectRepoPullRequest + "/comments/{commentId}")
                    fun retrieve(@Path("projectKey") projectKey: String,
                                 @Path("repositorySlug") repositorySlug: String,
                                 @Path("pullRequestId") pullRequestId: Long,
                                 @Path("commentId") commentId: Long): Observable<Comment>
                }
            }

            public interface Hooks {

                public enum class HookType {
                    PRE_RECIEVE,
                    POST_RECIEVE
                }

                @GET(projectRepoHooks)
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Query("type") type: HookType): Observable<Page<RepositoryHook>>

                @GET(projectRepoHook)
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Path("hookKey") hookKey: String,
                             @Query("type") type: HookType): Observable<Page<RepositoryHook>>

                @PUT(projectRepoHook + "/enabled")
                fun enable(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Path("hookKey") hookKey: String): Observable<RepositoryHook>

                @DELETE(projectRepoHook + "/enabled")
                fun disable(@Path("projectKey") projectKey: String,
                            @Path("repositorySlug") repositorySlug: String,
                            @Path("hookKey") hookKey: String): Observable<RepositoryHook>

                @POST(projectRepoHook + "/settings")
                fun update(@Path("projectKey") projectKey: String,
                           @Path("repositorySlug") repositorySlug: String,
                           @Path("hookKey") hookKey: String,
                           @Body repositoryHook: RepositoryHook): Observable<RepositoryHook>

                @GET(projectRepoHook + "/settings")
                fun retrieve(@Path("projectKey") projectKey: String,
                             @Path("repositorySlug") repositorySlug: String,
                             @Path("hookKey") hookKey: String): Observable<RepositoryHook>
            }

        }

    }

    public interface Repos {
        @GET(corePath + "/repos")
        fun retrieve(@Query("name") name: String,
                     @Query("projectname") projectName: String,
                     @Query("permission") permission: RepoPermission,
                     @Query("visibility") visibility: String): Observable<Page<Repository>>
    }

    public interface Tasks {
        @POST(corePath + "/tasks")
        fun create(@Body task: Task): Observable<Task>

        @DELETE(corePath + "/tasks/{taskId}")
        fun delete(@Path("taskId") taskId: Long)

        @PUT(corePath + "/tasks/{taskId}")
        fun update(@Path("taskId") taskId: Long,
                   @Body task: Task): Observable<Task>

        @GET(corePath + "/tasks/{taskId}")
        fun retrieve(@Path("taskId") taskId: Long): Observable<Task>
    }

    public interface Users{
        public object Avatar {
            public fun getUri(userSlug: String): Uri {
                return Uri.parse(corePath + "/users/" + userSlug + "/avatar.png")
            }
        }

        @GET(corePath + "/users?{filters}")
        fun all(@Path("filters") filters: String): Observable<Page<User>>

        @PUT(corePath + "/users")
        fun update(@Body user: User): Observable<User>

        @PUT(corePath + "/users/credentials")
        fun update(@Body credentials: Credentials)

        @GET(corePath + "/users/{userSlug}")
        fun single(@Path("userSlug") userSlug: String): Observable<User>

        @DELETE(corePath + "/users/{userSlug}/avatar.png")
        fun deleteAvatar(@Path("userSlug") userSlug: String)

        @POST(corePath + "/users/{userSlug}/avatar.png")
        fun updateAvatar(@Path("userSlug") userSlug: String)

        @GET(corePath + "/users/{userSlug}/settings")
        fun settings(@Path("userSlug") userSlug: String): Observable<UserSettings>

        @POST(corePath + "/users/{userSlug}/settings")
        fun updateSettings(@Path("userSlug") userSlug: String): Observable<UserSettings>
    }
}