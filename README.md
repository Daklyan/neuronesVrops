# Instructions
---
This program allow you to get the **alerts**, the **RAM**, the **CPU**, the **diskspace** and the **IOPS** of the VMs of a **VROps PCC** put them into a report and upload them to a **MySQL** database.

## Requirements

 * Java

## How to use it

To execute it just use this command line
```shell
  java -jar idc.jar
```

*Command line options*

-c : will launch the program to add a PCC to the configuration
```shell
  java -jar idc.jar -c
```

-t N : will launch the program and change the number of VMs in the tops of the report (There will be N VM in the top)
```shell
  java -jar idc.jar -t 22
```
---
## TO DO

* User interface
* Delete PCC option
* Backup of the database (for linux you can use [this](https://github.com/Daklyan/Rendu-stage))