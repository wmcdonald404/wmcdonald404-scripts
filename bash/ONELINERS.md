- get the fully resolved RHUI endpoint from the AWS instance medataservive (requires `bind-utils` for `/usr/bin/host`)

`$ host $(awk -F '/' '/REGION/ { print $3 }' /etc/yum.repos.d/redhat-rhui-client-config.repo | sed "s/REGION/$(curl -s http://169.254.169.254/latest/meta-data/placement/region)/")`
