# HTTP Batch Source

Description
-----------
The plugin fetches issues from JIRA in a parallel fashion.
JQL or existing JIRA filters or custom filters via configurations can be used 
in order to specify a subset of issues to get.


Properties
----------

### Basic

**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata, etc.

**Jira URL:** URL of JIRA instance.

**Filter Mode:** Mode which specifies which issues to fetch.
Possible values are:
- Basic
- JQL
- Jira Filter Id

***Basic Filter Mode***

Enables to specify basic filters using plugin properties.
Configurations which are lists has 'OR' relationship implied, which means issue
should only conform to one of the values specified.

Related configurations:

**Projects**: List of jira projects used for filtering.

**Issue types**: List of issue types used for filtering. 
E.g.: Sub-Task, Bug, Epic, Improvement, New Feature, Story, Task, etc.

**Statuses**: List of issue statuses used for filtering. 
E.g.: Open, In Progress, Reopened, Resolved, Closed, etc.

**Priorities**: List of priorities used for filtering.
E.g.: Minor, Major, Critical, Blocker, etc.

**Reporters**: List of reporters used for filtering.

**Assignees**: List of assignees used for filtering.

**Fix Versions**: List of fix versions used for filtering.

**Affected Versions**: List of affected versions used for filtering.

**Labels**: List of issue labels used for filtering.

**Last Update Start Date**: Minimal update date for the issues fetched.
Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 
'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.

**Last Update End Date**: Maximum update date for the issues fetched.
Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 
'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.


***JQL Filter Mode***

**JQL Query:** Query in JQL syntax used to filter the issues.
E.g.: `project = NETTY AND priority >= Critical AND fixVersion IN (4.2, 4.3) AND updateDate > -5d`
If empty will get all the issues from JIRA instance.


***Jira Filter Id Mode***

**Jira Filter Id:** Numerical id of an existing JIRA filter.

### Authentication

**Username:** Username used to authenticate to JIRA instance via basic authentication.

**Password:** Password used to authenticate to JIRA instance via basic authentication.
If both username and password are not set, JIRA will be accessed via anonymous user.

### Advanced

**Max Split Size:** Maximum number of issue which are fetched using a single request and 
processed in a single parallel split. Value of zero means unlimited. 
Please keep in mind that high values can result in timeouts due to large response sizes.

**Schema:** Output schema. Fields can be removed from it, if particular information is not needed.