![Burst](../../doc/burst_small.png "")


## Linux Kernel Config Issues

### vfs_cache_pressure
When loading datasets, we see that linux's page cache starts to exhibit some troubling memory hogging. The symptom is
that the memory associated with our mmap files is never released under normal conditions (it might be if we actually
used up the full OS memory). This causes us problems since we don't really know how much memory is available. 
 
[[https://major.io/2008/12/03/reducing-inode-and-dentry-caches-to-keep-oom-killer-at-bay/]]

This percentage value controls the tendency of the kernel to reclaim the memory which is used for caching of directory and inode objects.

From the manual:

    At the default value of vfs_cache_pressure=100 the kernel will attempt to reclaim dentries and inodes at a "fair" 
    rate with respect to pagecache and swapcache reclaim.  Decreasing vfs_cache_pressure causes the kernel to prefer 
    to retain dentry and inode caches. When vfs_cache_pressure=0, the kernel will never reclaim dentries and inodes 
    due to memory pressure and this can easily lead to out-of-memory conditions. Increasing vfs_cache_pressure beyond 100
    causes the kernel to prefer to reclaim dentries and inodes.
    
    Increasing vfs_cache_pressure significantly beyond 100 may have negative performance impact. Reclaim code needs to 
    take various locks to find freeable directory and inode objects. With vfs_cache_pressure=1000, it will look for 
    ten times more freeable objects than there are.

Check where it is currently set:

    > cat /proc/sys/vm/vfs_cache_pressure
    100
You should initially see the default value of __100__. That is not high enough. We want to change it to something
like __1000__.

    > echo 1000 | sudo tee /proc/sys/vm/vfs_cache_pressure
    1000
    
Setting this very high tells linux to more aggressively release cache memory to free memory.
 
To drop caches (and free up memory back to us) when we get into trouble we do:
    
    > echo 1 | sudo tee /proc/sys/vm/drop_caches
    1

When you have the right value, then you need to edit  `/etc/sysctl.conf` and add or modify...
    
    vfs_cache_pressure=1000
    
    
### Transparent Huge Pages
 * https://blogs.oracle.com/linux/entry/performance_issues_with_transparent_huge
 * https://blogs.oracle.com/linuxkernel/entry/performance_impact_of_transparent_huge
 * https://oracle-base.com/articles/linux/configuring-huge-pages-for-oracle-on-linux-64
 * https://access.redhat.com/solutions/46111 

The kernel attempts to create a coarser grain TLB quantum for virtual memory management but seems to create a very
high overhead for processes that promote small to huge page grains

    # cat /sys/kernel/mm/transparent_hugepage/enabled
    [always] madvise never

you are looking to see [never].

Here we have two options - disable THP all together or just disable defragging.

       echo never > /sys/kernel/mm/transparent_hugepage/enabled
       echo never > /sys/kernel/mm/transparent_hugepage/defrag

And test to see if it worked

        cat /sys/kernel/mm/transparent_hugepage/enabled
               always madvise [never]
        cat /sys/kernel/mm/transparent_hugepage/defrag
               always madvise [never]

Unfortunately `'sysctl.conf'` is not an option for this. Extra credit to put it in `'grub.conf'`. We can't do that either.
