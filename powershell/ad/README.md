# Powershell AD Domain Controller Setup

Some simple steps (intended for futher automation) to set up a quick AD controller (or three). 

# Nostromo Active Directory
## Virtual Machine Host and Network Identity
It’s assumed that Windows Server 2016 Core has been installed using defaults, and an Administrator password has been set. Once the OS is installed next it is necessary to:
-	Configure networking
    -	List interfaces, note the IPv4 Ethernet interface index
    -	Set the IP address
    -	Set the netmask
    -	Set the default gateway
    -	Set the DNS for client resolver lookups.
    -	Set the hostname
-	Set up time sync
-	Restart the host

```
PS> Get-NetIPInterface -InterfaceAlias Ethernet0 -AddressFamily IPv4
PS> New-NetIPaddress -InterfaceIndex 2 -IPAddress 192.168.0.20 -PrefixLength 24 -DefaultGateway 192.168.0.1
PS> Set-DNSClientServerAddress –InterfaceIndex 2 -ServerAddresses 192.168.0.8
PS> Rename-Computer -NewName ad01
PS> w32tm /config /manualpeerlist:pool.ntp.org /syncfromflags:MANUAL
PS> Stop-Service w32time
PS> Start-Service w32time
PS> Restart-Computer
```
## Configure Remote Connectivity
At this stage, it is possible to configure remote access to the Windows host. This can simplify subsequent steps by reducing operator interaction (typing), and allowing cut/paste. There are a couple of methods to achieve this:
-	Windows Remote Server Administration Tools
-	Remote Desktop
-	Powershell Remoting

### Remote Server Administration Tools
```
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts 192.168.0.20
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts 192.168.0.21
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01.nostromo.com
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01.sulaco.com
```
### Remote Desktop
```
PS> $ComputerName = [System.Net.Dns]::GetHostName()
PS> (Get-WmiObject -class Win32_TSGeneralSetting -Namespace root\cimv2\terminalservices -ComputerName $ComputerName -Filter "TerminalName='RDP-tcp'").SetUserAuthenticationRequired(0)
```

### Powershell Remoting
```
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts 192.168.0.20
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts 192.168.0.21
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01.nostromo.com
PS> set-Item -Force  wsman:\localhost\Client\TrustedHosts ad01.sulaco.com
```

Note: If "WinRM Negotiate authentication error" still persist it’s possible to trust ‘*’ as a temporary workaround.

## Configure Active Directory
Now it should be possible to assign the machine a role as an AD domain controller (DC). The following Powershell will install the AD Domain Services Windows feature and its dependencies, invoke the deployment module, then configure the target system as an AD DC (and configure DNS for the domain).

```
PS> Install-WindowsFeature AD-Domain-Services -IncludeManagementTools
PS> Import-Module ADDSDeployment
PS> Install-ADDSForest `
-CreateDnsDelegation:$false `
-DatabasePath "C:\Windows\NTDS" `
-DomainMode "WinThreshold" `
-DomainName "nostromo.com" `
-DomainNetbiosName "NOSTROMO" `
-ForestMode "WinThreshold" `
-InstallDns:$true `
-LogPath "C:\Windows\NTDS" `
-NoRebootOnCompletion:$false `
-SysvolPath "C:\Windows\SYSVOL" `
-Force:$true `
-SafeModeAdministratorPassword (ConvertTo-SecureString –String 'USCSSNostromo!' -AsPlainText -Force)
```

The machine will reboot.
### Validate Configuration
```
PS> Get-Service adws,kdc,netlogon,dns
PS> Get-SmbShare
PS> dcdiag /q
PS> Resolve-DnsName ad01.nostromo.com
PS> Resolve-DnsName nostromo.com
```

## Configure Users and Groups
Active Directory Security Groups can be used to group domain users with similar roles, departments, organisational responsibilities or to reflect other organisational concerns. Permissions can then be assigned at the group level, reducing the management overhead as users join, change role or department, or leave.

Create domain security groups, domain users and assign those users to the groups. 
### Create Directories
```
PS> New-ADGroup -Name "Officers" -SamAccountName Officers -GroupCategory Security -GroupScope Global -DisplayName "Bridge Officers" -Path "CN=Users,DC=Nostromo,DC=Com" -Description "Members of Bridge Officers"

PS> New-ADGroup -Name "Engineers" -SamAccountName Engineers -GroupCategory Security -GroupScope Global -DisplayName "Engineering Crew" -Path "CN=Users,DC=Nostromo,DC=Com" -Description "Members of Engineering Crew"
Create Users
PS> $Attributes = @{
   Enabled = $true
   ChangePasswordAtLogon = $false
   UserPrincipalName = "dallas@nostromo.com"
   Name = "dallas"
   GivenName = "Captain"
   Surname = "Dallas"
   DisplayName = "Captain Dallas"
   Office = "Bridge"
   AccountPassword = "Thatfigures." | ConvertTo-SecureString -AsPlainText -Force
}

PS> New-ADUser @Attributes

PS> $Attributes = @{
   Enabled = $true
   ChangePasswordAtLogon = $false
   UserPrincipalName = "kane@nostromo.com"
   Name = "kane"
   GivenName = "XO"
   Surname = "Kane"
   DisplayName = "XO Kane"
   Office = "Bridge"
   AccountPassword = "Sillyquestion?" | ConvertTo-SecureString -AsPlainText -Force
}

PS> New-ADUser @Attributes

PS> $Attributes = @{
   Enabled = $true
   ChangePasswordAtLogon = $false
   UserPrincipalName = "parker@nostromo.com"
   Name = "parker"
   GivenName = "Chief"
   Surname = "Parker"
   DisplayName = "Chief Parker"
   Office = "Engineering"
   AccountPassword = "Howyadoin?" | ConvertTo-SecureString -AsPlainText -Force
}

PS> New-ADUser @Attributes
```
### Add Users to Security Groups
```
PS> Add-ADGroupMember -Identity Officers -Members dallas, kane
PS> Add-ADGroupMember -Identity Engineers -Members parker
```

### Add DNS Records
Add delegation for idm.nostromo.com
```
PS> Add-DnsServerZoneDelegation -Name "nostromo.com" -ChildZoneName "idm" -NameServer "idm01.idm.nostromo.com" -IPAddress 192.168.0.22 -PassThru -Verbose
```

