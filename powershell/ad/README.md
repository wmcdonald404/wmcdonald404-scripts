# Powershell AD Domain Controller Setup

Some simple steps (intended for futher automation) to set up a quick AD controller (or three). 


1. Provision a VM (steps for simulating with EC2 instances TBC).

For Windows Nano: 1 x vCPU, 2GB vmem, 40GB vmdk.
For the Desktop Experience: 2 vCPU & 4GB vmem, 40GB vmdk.

2. Install Windows 2016 Server Core x64

3. Set Administrator password

4. Network configuration, configure:
  IP address
  Netmask
  default gateway
  servers for client resolver lookups.
  Hostname
  Enable Remote Desktop Access for users in the Administrators group
  Open Windows Firewall ports for Remote Desktop Access

5. Install VMware Tools (POWERSHELL TODO?)

6. Enable hypervisor time sync (for lab convenience, use proper time synchronization in real world scenarios.)

7. Restart the host

Steps 1-7:

```
PS> Get-NetIPInterface
PS> New-NetIPaddress -InterfaceIndex 2 -IPAddress 192.168.0.20 -PrefixLength 24 -DefaultGateway 192.168.0.1
PS> Set-DNSClientServerAddress -InterfaceIndex 2 -ServerAddresses 192.168.0.8
PS> Rename-Computer -NewName ad01
PS> Set-ItemProperty -Path "HKLM:\System\CurrentControlSet\Control\Terminal Server" -Name "fDenyTSConnections" -Value 0
PS> Enable-NetFirewallRule -DisplayGroup "Remote Desktop"
PS> Restart-Computer
 
 
w32tm /config /manualpeerlist:pool.ntp.org /syncfromflags:MANUAL
Stop-Service w32time
Start-Service w32time
 
w32tm /query /status
```

8. In Server Manager, open a Powershell session to the machine destined to be the domain controller
  Right-click 192.168.0.20
  Windows Powershell
  Username: Administrator
  Password: <admin-password>

9. Create the Domain Controller

```
#
# Windows PowerShell script for AD DS Deployment
#
Install-WindowsFeature AD-Domain-Services -IncludeManagementTools
Import-Module ADDSDeployment
Install-ADDSForest `
-CreateDnsDelegation:$false `
-DatabasePath "C:\Windows\NTDS" `
-DomainMode "WinThreshold" `
-DomainName "example004.com" `
-DomainNetbiosName "EXAMPLE004" `
-ForestMode "WinThreshold" `
-InstallDns:$true `
-LogPath "C:\Windows\NTDS" `
-NoRebootOnCompletion:$false `
-SysvolPath "C:\Windows\SYSVOL" `
-Force:$true
```

Enter the Administrator password when prompted for SafeModeAdministratorPassword:

10. Refresh the host credentials in Server Manager
  Right-click 192.168.0.20
  Manage as
  Username: Administrator@example004.com
  Password: <admin-password>
  Validate running services

11. Validate running services

PS> Get-Service adws,kdc,netlogon,dns


