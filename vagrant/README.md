# Vagrant Notes

# Setting up Vagrant with the Libvirt Provider
1. Check for/enable Intel-VT / AMD-V CPU virtualisation extensions. (Don't skip this, they're disabled by default on most laptop systems and will cause largely silent failure if not enabled.)
```
$ egrep '^flags.*(vmx|svm)' /proc/cpuinfo
```
2. install libvirt
```
# dnf -y install @virtualization
```
3. Install libvirt and some dependencies required for vagrant-libvirt later.
```
# dnf -y install gcc libvirt libvirt-devel libxml2-devel make ruby-devel libguestfs-tools
```
(nb: find out why the requisites aren't depended on by vagrant-libvirt? Maybe some variant on https://bugzilla.redhat.com/show_bug.cgi?id=1523296 ?)

4. install vagrant & the vagrant libvirt plugin
```
# dnf install vagrant vagrant-libvirt
```
5. Enable/start libvirtd.service
```
# systemctl enable libvirtd.service
```
6. Setup the Vagrant libvirt plugin (May or may not be required, need to verify on a clean installation.)
```
$  vagrant plugin install vagrant-libvirt
```
7. Quick hack around https://bugzilla.redhat.com/show_bug.cgi?id=1187019
```
# usermod -G libvirt <username>
```
## Set up a Fedora Vagrant Box

1. Add a fedora vagrant box base image (https://app.vagrantup.com/fedora/boxes/37-cloud-base)
```
$ vagrant box add fedora/37-cloud-base --provider libvirt
```

2. Init a box and start up
```
$ mkdir working/vagrant/fedora-scratch
$ cd $_
$ vagrant init fedora/37-cloud-base --box-version 37.20221105.0
$ vagrant up
$ vagrant halt
```

Notes:
If `vagrant up` fails with the following error:
> Call to virConnectListAllNetworks failed:

Run the following as a temporary mitigation (proper root-cause required):
```
sudo virsh net-list --all
```
See: https://github.com/hashicorp/vagrant/issues/12605

## Single Fedora Vagrant Box with an Ansible Provisioner
To run a Vagrant box with a simple set of defaults and trigger the Ansible provisioner:

1. Review the Vagrant file in: `wmcdonald404-scripts/vagrant/fedora-scratch/Vagrantfile`

2. Review the simple playbook in: `wmcdonald404-scripts/vagrant/fedora-scratch/playbook.yml`

3. Check the Vagrant status
```
$ vagrant status
Current machine states:

default                   shutoff (libvirt)

The Libvirt domain is not running. Run `vagrant up` to start it.
```
4. Start ("up") the Vagrant box, see that the Ansible provisioner runs
```
$ vagrant up
Bringing machine 'default' up with 'libvirt' provider...
==> default: Checking if box 'fedora/37-cloud-base' version '37.20221105.0' is up to date...
<vagrant output truncated>
<ansible output truncated>
PLAY RECAP *********************************************************************
default                    : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```

5. Subsequent local Ansible runs to test new/additional playbook or role functionality can be triggered using the keys and inventory:
```
$ export ANSIBLE_INVENTORY=~/repos/wmcdonald404-scripts/vagrant/fedora-scratch/.vagrant/provisioners/ansible/inventory/vagrant_ansible_inventory

$ ansible-inventory --graph
@all:
  |--@ungrouped:
  |--@vagrant:
  |  |--default
```

## Multiple Fedora Vagrant Boxes with a single Ansible Provisioner run
By default in most scenarios, Vagrant will trigger a provisioner run once for every Vagrant box created (dependent on the structure of the Vagrantfile). The following process outlines the steps to:
- Create multiple Vagrant boxes
- Once created, trigger a single Ansible provisioner run

1. Review the Vagrant file in: `wmcdonald404-scripts/vagrant/fedora-multi/Vagrantfile`

2. Review the simple playbook in: `wmcdonald404-scripts/vagrant/fedora-multi/playbook.yml`

3. Check the Vagrant status
```
$ vagrant status
Current machine states:

node0                     not created (libvirt)
node1                     not created (libvirt)
node2                     not created (libvirt)

This environment represents multiple VMs. The VMs are all listed
above with their current state. For more information about a specific
VM, run `vagrant status NAME`.
```
4. Start ("up") the Vagrant box, see that the Ansible provisioner runs
```
$ vagrant up
Bringing machine 'node0' up with 'libvirt' provider...
Bringing machine 'node1' up with 'libvirt' provider...
Bringing machine 'node2' up with 'libvirt' provider...
==> node0: Checking if box 'fedora/37-cloud-base' version '37.20221105.0' is up to date...
==> node2: Checking if box 'fedora/37-cloud-base' version '37.20221105.0' is up to date...
==> node1: Checking if box 'fedora/37-cloud-base' version '37.20221105.0' is up to date...
<vagrant output truncated>
PLAY [Vagrant post-provision] **************************************************

TASK [Gathering Facts] *********************************************************
ok: [node1]
ok: [node0]
ok: [node2]

TASK [Debug vars for hosts] ****************************************************
ok: [node0] => {
    "ansible_play_hosts": [
        "node0",
        "node1",
        "node2"
    ]
}
ok: [node1] => {
    "ansible_play_hosts": [
        "node0",
        "node1",
        "node2"
    ]
}
ok: [node2] => {
    "ansible_play_hosts": [
        "node0",
        "node1",
        "node2"
    ]
}

PLAY RECAP *********************************************************************
node0                      : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
node1                      : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
node2                      : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```

> Note: The Ansible playbook has run once across all nodes in the inventory group.

# References:

- https://computingforgeeks.com/using-vagrant-with-libvirt-on-linux/
- https://app.vagrantup.com/fedora/boxes/37-cloud-base
- https://alt.fedoraproject.org/en/cloud/
- https://app.vagrantup.com/fedora/
- https://download.fedoraproject.org/pub/fedora/linux/releases/37/Cloud/x86_64/images/Fedora-Cloud-Base-Vagrant-37-1.7.x86_64.vagrant-libvirt.box
- https://github.com/vagrant-libvirt/vagrant-libvirt#installing
- https://developer.fedoraproject.org/tools/vagrant/vagrant-libvirt.html
- https://fedoraproject.org/wiki/Changes/LibvirtModularDaemons
- https://fedoramagazine.org/setting-up-a-vm-on-fedora-server-using-cloud-images-and-virt-install-version-3/
- https://blog.while-true-do.io/cloud-init-getting-started/
- https://opensource.com/article/21/10/vagrant-libvirt

## Ordering the Vagrant Ansible provisioner to run once:
- https://everythingshouldbevirtual.com/automation/virtualization/vagrant-ansible-provisioning-multi-nodes/
- https://stackoverflow.com/questions/54468546/how-to-run-an-ansible-playbook-on-a-specific-vagrant-host

