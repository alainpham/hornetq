/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.messaging.core.remoting;

import org.jboss.messaging.core.exception.MessagingException;

/**
 *
 * This is class is a simple way to intercepting server calls on JBoss Messaging.
 * 
 * To Add this interceptor, you have to modify jbm-configuration.xml, or call RemotingServiceImpl.addInterceptor manually.
 * 
 * If you deploy any Interceptor as a POJO on the Microcontainer, RemotingServiceImpl.addInterceptor is called automagically.
 *  
 * @author clebert.suconic@jboss.com
 */
public interface Interceptor
{   
   void intercept(Packet packet) throws MessagingException;
}
