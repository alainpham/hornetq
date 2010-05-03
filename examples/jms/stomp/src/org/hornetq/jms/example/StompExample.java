/*
 * Copyright 2010 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.hornetq.jms.example;

import java.io.OutputStream;
import java.net.Socket;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.hornetq.common.example.HornetQExample;

/**
 * An example where a client will send a Stomp message on a TCP socket
 * and consume it from a JMS MessageConsumer.
 *
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class StompExample extends HornetQExample
{
   private static final String END_OF_FRAME = "\u0000";

   public static void main(final String[] args)
   {
      new StompExample().run(args);
   }

   @Override
   public boolean runExample() throws Exception
   {
      Connection connection = null;
      InitialContext initialContext = null;

      try
      {
         // Step 1. Create a TCP socket to connect to the Stomp port
         Socket socket = new Socket("localhost", 61613);

         // Step 2. Send a CONNECT frame to connect to the server
         String connectFrame = "CONNECT\n" + 
            "login: guest\n" +
            "passcode: guest\n" +
            "request-id: 1\n" +
            "\n" +
            END_OF_FRAME;
         sendFrame(socket, connectFrame);

         // Step 3. Send a SEND frame (a Stomp message) to the
         // jms.queue.exampleQueue address with a text body
         String text = "Hello, world from Stomp!";
         String message = "SEND\n" + 
            "destination: jms.queue.exampleQueue\n" +
            "\n" +
            text +
            END_OF_FRAME;
         sendFrame(socket, message);
         System.out.println("Sent Stomp message: " + text);

         // Step 4. Send a DISCONNECT frame to disconnect from the server
         String disconnectFrame = "DISCONNECT\n" +
            "\n" +
            END_OF_FRAME;
         sendFrame(socket, disconnectFrame);

         // Step 5. Slose the TCP socket
         socket.close();

         // We will now consume from JMS the message sent with Stomp.

         // Step 6. Create an initial context to perform the JNDI lookup.
         initialContext = getContext(0);

         // Step 7. Perform a lookup on the queue and the connection factory
         Queue queue = (Queue)initialContext.lookup("/queue/exampleQueue");
         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");

         // Step 8.Create a JMS Connection, Session and a MessageConsumer on the queue
         connection = cf.createConnection();
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer consumer = session.createConsumer(queue);

         // Step 9. Start the Connection
         connection.start();

         // Step 10. Receive the message
         TextMessage messageReceived = (TextMessage)consumer.receive(5000);
         System.out.println("Received JMS message: " + messageReceived.getText());

         return true;
      }
      finally
      {
         // Step 11. Be sure to close our JMS resources!
         if (initialContext != null)
         {
            initialContext.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }

   private static void sendFrame(Socket socket, String data) throws Exception
   {
      byte[] bytes = data.getBytes("UTF-8");
      OutputStream outputStream = socket.getOutputStream();
      for (int i = 0; i < bytes.length; i++)
      {
         outputStream.write(bytes[i]);
      }
      outputStream.flush();
   }

}
