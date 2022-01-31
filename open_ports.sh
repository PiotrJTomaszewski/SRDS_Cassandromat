#!/bin/bash

iptables -A INPUT -p tcp --dport 9042 -j ACCEPT
iptables -A INPUT -p tcp --dport 9043 -j ACCEPT
iptables -A INPUT -p tcp --dport 9044 -j ACCEPT
