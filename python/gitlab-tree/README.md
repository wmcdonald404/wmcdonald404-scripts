## About
A simple Python script to build a tree view of Gitlab groups and projects/repositories. See [Gitlab Group and Project Tree Walk](https://wmcdonald404.github.io/github-pages/2024/04/28/16-34-48-gitlab-group-and-project-tree-walk.html) for more.

## Get Started

### Prerequisites

The bulk of Python modules should all be in [The Python Standard Library](https://docs.python.org/3/library/index.html).
- `argparse`
- `json`
- `os`
- `pathlib`
- `sys`
- `time`
- `yaml`

Exceptions *(below)* will typically be available through the OS package manager.
- `python3-pyyaml`
- `python3-gitlab`
- `python3-anytree`


**Fedora:**
`$ sudo dnf -y install python3-pyyaml python3-gitlab python3-anytree`

**Ubuntu:**
`python3-yaml python3-gitlab`

TODO: Find anytree packaged, or replace with a Python Standard Library.

### Installation

To install into a local folder on the `PATH`:
```
 $ mkdir ~/.local/bin/
 $ curl -s https://raw.githubusercontent.com/wmcdonald404/wmcdonald404-scripts/master/python/gitlab-tree/glgtree -o ~/.local/bin/glgtree
 $ chmod u+x $_
 ```

## Usage

The basic usage is:
```
wmcdonald@fedora:~$ glgtree -h
usage: glgtree [-h] [-d | -j | -t] [-f] [-g GROUP]

Build a tree of Gitlab groups and projects

options:
  -h, --help            show this help message and exit
  -d, --dict            Output as Python dictionary
  -j, --json            Output as JSON
  -t, --tree            Output as tree (default)
  -f, --force           Force cache refresh
  -g GROUP, --group GROUP
                        Gitlab group to base output from
```

## Caching
By default the script will check for, and use a cached copy of the JSON data retrived from the Gitlab API under `~/.config/glgtree/cache.json`. If there's no cached data, fresh data will be retrived from the API.

This data will be refreshed after 24 hours, a refresh can be forced with `-f` or `--force` arguments.


## Output
Default rendering:
```
wmcdonald@fedora:~$ glgtree 
📁 demo-topgroup (86427305)
├── 📁 demo-subgroup1 (86427310)
│   ├── 📁 subgroup1-team1 (86427383)
│   │   ├── 📁 team1-squad-a (86427510)
│   │   │   ├── 📗 squad-a-memelords (57340128)
│   │   │   ├── 📗 squad-a-app1 (57340122)
│   │   │   ├── 📗 squad-a-build (57340114)
│   │   │   └── 📗 squad-a-infra (57340105)
│   │   ├── 📁 team1-squad-b (86427519)
│   │   ├── 📗 team1-repo2 (57339925)
│   │   └── 📗 team1-repo1 (57339921)
│   └── 📁 subgroup1-team2 (86427393)
│       ├── 📗 team2-repo2 (57339964)
│       └── 📗 team2-repo1 (57339959)
├── 📁 demo-subgroup2 (86427321)
│   ├── 📗 subgroup2-app2 (57339899)
│   └── 📗 subgroup2-app1 (57339887)
└── 📗 demo-top-project (57528685)
```
Subgroup:
```
wmcdonald@fedora:~$ glgtree -g 86427321
📁 demo-subgroup2 (86427321)
├── 📗 subgroup2-app2 (57339899)
└── 📗 subgroup2-app1 (57339887)
```
JSON output:
```
wmcdonald@fedora:~$ glgtree -g 86427321 -j
{"id": 86427321, "name": "demo-subgroup2", "parent_id": 86427305, "object_kind": "group", "object_icon": "\ud83d\udcc1", "children": [{"id": 57339899, "name": "subgroup2-app2", "parent_id": 86427321, "object_kind": "project", "object_icon": "\ud83d\udcd7"}, {"id": 57339887, "name": "subgroup2-app1", "parent_id": 86427321, "object_kind": "project", "object_icon": "\ud83d\udcd7"}]}
```
Parsed through `jq`:
```
wmcdonald@fedora:~$ glgtree -g 86427321 -j | jq
{
  "id": 86427321,
  "name": "demo-subgroup2",
  "parent_id": 86427305,
  "object_kind": "group",
  "object_icon": "📁",
  "children": [
    {
      "id": 57339899,
      "name": "subgroup2-app2",
      "parent_id": 86427321,
      "object_kind": "project",
      "object_icon": "📗"
    },
    {
      "id": 57339887,
      "name": "subgroup2-app1",
      "parent_id": 86427321,
      "object_kind": "project",
      "object_icon": "📗"
    }
  ]
}
```

## TODO
Areas for improvement:
- optionally source GITLAB_GROUP from environment
- ~~decompose some of the main logic into separate functions~~
- ~~Add disk caching/refresh?~~
- separate out CLI from "API"
- Modularise
- Write pytest tests
- cache per-organisation/id?

## TOFIX:
- ~~branch/leaf/node selection after cache addition~~
- performance (investigate graphql endpoint instead of REST? (https://stackoverflow.com/a/71313528))
    note: gitlab_connection.projects.list(visibility='private', get_all=True)
      ~500 projects takes 30 secs 
      ~10 projects takes 1.1 secs
      terminal output makes little difference (few ms.)
    note: gitlab_connection.groups.list(get_all=True, visibility='private')
      ~120 groups adds 10 secs
      ~7 groups adds 0.4 sec
    comparison: the previous recursive glgtree version takes 2m14 for the full 648 object group/project tree

## Contributing
PRs welcome. IANAProgrammer so much of this is likely to be janky and inefficient.
