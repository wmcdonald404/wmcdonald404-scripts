| Command | What it does |
|--- |--- |
|`$ host $(awk -F '/' '/REGION/ { print $3 }' /etc/yum.repos.d/redhat-rhui-client-config.repo | sed "s/REGION/$(curl -s http://169.254.169.254/latest/meta-data/placement/region)/")`| Get the fully resolved RHUI endpoint from the AWS instance metadata service[^1] (requires `bind-utils` for `/usr/bin/host`) |
| `$ curl -s http://169.254.169.254/latest/meta-data/public-hostname`| Get the public hostname while logged into an instance (again, from the instance metadata service[^1]) |
| :set sb, :term | Forks a new terminal session in a Vim session, ersatz tmux/screen |





[^1]: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html
