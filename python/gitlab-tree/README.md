## About
A simple Python script to build a tree view of Gitlab groups and projects/repositories. See [Gitlab Group and Project Tree Walk](https://wmcdonald404.github.io/github-pages/2024/04/28/16-34-48-gitlab-group-and-project-tree-walk.html) for more.

## Get Started

### Prerequisites

The bulk of Python modules should all be in [The Python Standard Library](https://docs.python.org/3/library/index.html).
- `sys`
- `argparse`
- `os`
- `json`

Exceptions *(below)* will typically be available through the OS package manager.
- `python3-pyyaml`
- `python3-gitlab`
- `python3-anytree`


**Fedora:**
`$ sudo dnf -y install python3-pyyaml python3-gitlab python3-anytree`

**Ubuntu:**
`TODO`

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
usage: glgtree [-h] [-d | -j | -t] [-g GROUP]

Build a tree of Gitlab groups and projects

options:
  -h, --help            show this help message and exit
  -d, --dictionary      Output as Python dictionary
  -j, --json            Output as JSON
  -t, --tree            Output as tree (default)
  -g GROUP, --group GROUP
                        Gitlab group to base output from
```

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

## Contributing

PRs welcome. IANAProgrammer so much of this is likely to be janky and inefficient.
