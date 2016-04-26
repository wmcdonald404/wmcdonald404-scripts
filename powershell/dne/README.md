# Toggling the DNE Lightweight Filter

The DNE Lightweight Filter is required for Cisco IPSEC VPN client connectivity. However having DNE enabled on systems running VMs can prevent TCP connections from the host to brdiged interfaces on guests it's hosting. (Connections from guest to guest, or from external hosts work fine.)

As a quick workaround, DNE can be enabled and disabled from an Administrative PowerShell session:

1. Identify the appropriate network adapter

  ```
  PS C:\Users\Demo\Desktop> Get-NetAdapter
  
  Name                      InterfaceDescription                    ifIndex Status       MacAddress             LinkSpeed
  ----                      --------------------                    ------- ------       ----------             ---------
  VMware Network Adapte...8 VMware Virtual Ethernet Adapter for ...       7 Up           00-50-56-C0-00-08       100 Mbps
  VMware Network Adapte...1 VMware Virtual Ethernet Adapter for ...       5 Up           00-50-56-C0-00-01       100 Mbps
  Ethernet                  Intel(R) Ethernet Connection (2) I218-V      13 Up           78-24-AF-43-D1-C5         1 Gbps
  Ethernet 2                Cisco Systems VPN Adapter for 64-bit...      19 Disabled     00-05-9A-3C-78-00       100 Mbps
  
  PS C:\Users\Demo\Desktop> Get-NetAdapter -name Ethernet | Format-List
  
  Name                       : Ethernet
  InterfaceDescription       : Intel(R) Ethernet Connection (2) I218-V
  InterfaceIndex             : 13
  MacAddress                 : 78-24-AF-43-D1-C5
  MediaType                  : 802.3
  PhysicalMediaType          : 802.3
  InterfaceOperationalStatus : Up
  AdminStatus                : Up
  LinkSpeed(Gbps)            : 1
  MediaConnectionState       : Connected
  ConnectorPresent           : True
  DriverInformation          : Driver Date 2015-03-20 Version 12.12.50.6 NDIS 6.30
  ```

2. Examine the protocols bound to the adapter

  ```
  PS C:\Users\Demo\Desktop> Get-NetAdapterBinding -name Ethernet
   
  Name                           DisplayName                                        ComponentID          Enabled     
  ----                           -----------                                        -----------          -------     
  Ethernet                       DNE LightWeight Filter                             dni_dne              False       
  Ethernet                       Microsoft Network Adapter Multiplexor Protocol     ms_implat            False       
  Ethernet                       Microsoft LLDP Protocol Driver                     ms_lldp              True        
  Ethernet                       Link-Layer Topology Discovery Mapper I/O Driver    ms_lltdio            True        
  Ethernet                       Client for Microsoft Networks                      ms_msclient          True        
  Ethernet                       QoS Packet Scheduler                               ms_pacer             True        
  Ethernet                       Link-Layer Topology Discovery Responder            ms_rspndr            True        
  Ethernet                       File and Printer Sharing for Microsoft Networks    ms_server            True        
  Ethernet                       Internet Protocol Version 4 (TCP/IPv4)             ms_tcpip             True        
  Ethernet                       Internet Protocol Version 6 (TCP/IPv6)             ms_tcpip6            True        
  Ethernet                       VMware Bridge Protocol                             vmware_bridge        True        
  ```
  
  Note: Again, use `Get-NetAdapterBinding -name Ethernet |  Format-List` for more information on each individual entry

3. Disable DNE on the appropriate adapter

  ```Set-NetAdapterBinding -name Ethernet -DisplayName "DNE LightWeight Filter" -Enabled $false```
  
4. Re-enable DNE on the adapter when done
  
  ```Set-NetAdapterBinding -name Ethernet -DisplayName "DNE LightWeight Filter" -Enabled $true```
