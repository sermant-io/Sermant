# Contributing to Sermant

Welcome to Sermant! This document is a guideline about how to contribute to Sermant.

If you find something incorrect or missing, please leave comments / suggestions.

## Before You Get Started

### Code of Conduct

Please make sure to read and observe our [Code of Conduct](https://github.com/sermant-io/Sermant/tree/develop/CODE_OF_CONDUCT.md).

### Open Source Contribution Agreement

The Sermant community adopts DCO ([Developer Certificate of Origin]( https://developercertificate.org/ )) as its open source contribution protocol, if you want to participate in community contributions, you need to follow the following steps.

#### Register GitHub Account

If you do not have a GitHub account, you need to log in to GitHub and register using an email address. The email address is used to sign DCO and configure the SSH public key.

#### Sign DCO

DCO ([Developer Certificate of Origin]( https://developercertificate.org/ ))  is a lightweight open source contribution protocol for open source contributors to prove their right to grant projects access to their code.

When submitting code commit using Git CLI, you can use the [-s parameter](https://git-scm.com/docs/git-commit) to add a signature. Examples of usage are as follows:

```Shell

$git commit -s -m 'This is my commit message'

```

The signature will be part of the submitted commit message in the format:

```
This is my commit message

Signed off by: Full Name<email>

```

> - If you use IntelliJ IDEA to submit code, you can check the `Sign-off commit` option in the Commit Change tool box to attach signature information every time you commit. Please refer to the [IntelliJ IDEA User Documentation](https://www.jetbrains.com/help/idea/commit-changes-dialog.html#2ddf66ea)  for specific operations.
> - If you use Visual Studio Code to submit code, you can check the `Git: Always Sign Off` option in Settings to attach signature information every time you commit. Please refer to the [related pull request of Visual Studio Code ](https://www.jetbrains.com/help/idea/commit-changes-dialog.html#2ddf66ea) for specific operations.

Please confirm that each time you submit a commit, you correctly add a signature and sign the DCO in the above way. Otherwise, your submitted code will not be accepted and integrated into our  repository.

## Contributing

Sermant welcome new participants of any role, including user, contributor, committer and PMC.

We encourage new comers actively join in Sermant projects and involving from user role to committer role, and even PMC role. In order to accomplish this, new comers needs to actively contribute in Sermant project. The following paragraph introduce how to contribute in Sermant way.

#### Open / Pickup An Issue for Preparation

If you find a typo in document, find a bug in code, or want new features, or want to give suggestions, you can [open an issue on GitHub](https://github.com/sermant-io/Sermant/issues/new) to report it.

We strongly value documentation and integration with other projects such as Spring Cloud, Kubernetes, Dubbo, etc. We are very glad to work on any issue for these aspects.

Please note that any PR must be associated with a valid issue. Otherwise the PR will be rejected.

#### Begin Your Contribute

Now if you want to contribute, please create a new pull request.

We use the `develop` branch as the development branch, which indicates that this is a unstable branch.

Further more, our branching model complies to [A successful Git branching model](https://nvie.com/posts/a-successful-git-branching-model/). We strongly suggest new comers walk through the above article before creating PR.

Now, if you are ready to create PR, here is the workflow for contributors:

1.  Fork to your own

2.  Clone fork to local repository

3.  Create a new branch and work on it

4.  Keep your branch in sync

5.  Commit your changes (make sure your commit message concise)

6.  Push your commits to your forked repository

7.  Create a pull request to **develop** branch.


When creating pull request:

1.  Please follow [the pull request template](https://github.com/sermant-io/Sermant/tree/develop/.github/pull_request_template.md).

2.  Please create the request to **develop** branch.

3.  Please make sure the PR has a corresponding issue.

4.  If your PR contains large changes, e.g. component refactor or new components, please write detailed documents about its design and usage.

5.  Note that a single PR should not be too large. If heavy changes are required, it's better to separate the changes to a few individual PRs.

6.  After creating a PR, one or more reviewers will be assigned to the pull request.

7.  Before merging a PR, squash any fix review feedback, typo, merged, and rebased sorts of commits. The final commit message should be clear and concise.


If your PR contains large changes, e.g. component refactor or new components, please write detailed documents about its design and usage.

### Code Review Guidance

Committers will rotate reviewing the code to make sure all the PR will be reviewed timely and by at least one committer before merge. If we aren't doing our job (sometimes we drop things). And as always, we welcome volunteers for code review.

Some principles:

-   Readability - Important code should be well-documented. API should have Javadoc. Code style should be complied with the existing one.

-   Elegance - New functions, classes or components should be well designed.

-   Testability - 80% of the new code should be covered by unit test cases.

-   Maintainability - to be done.

### Testing

There are multiple types of tests.
The location of the test code varies with type, as do the specifics of the environment needed to successfully run the test:

* Unit: These confirm that a particular function behaves as intended. Unit test source code can be found adjacent to the corresponding source code within a given package. These are easily run locally by any developer.
* Integration: These tests cover interactions of package components or interactions between Sermant components and host applications.

Continuous integration will run these tests on PRs.

### Community Expectations

Sermant is a community project driven by its community which strives to promote a healthy, friendly and productive environment. The goal of the community is to build a plugin development ecosystem to assist developers in more easily creating service governance functionalities without interfering with the application's source code. It requires the support of a community with similar aspirations.

- See [Community Membership](https://github.com/sermant-io/Sermant/blob/develop/community-membership.md) for a list of various community roles. With gradual contributions, one can move up in the chain.
