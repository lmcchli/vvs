var sb = new java.lang.StringBuffer();

// Now add some stuff to the buffer.
sb.append("hi, mom");
sb.append(3);	// this will add "3.0" to the buffer since all JS numbers
		// are doubles by default
sb.append(true);

// Now print it out. (The toString() method of sb is automatically called
// to convert the buffer to a string.)
// Should print "hi, mom3.0true".
print(sb);
