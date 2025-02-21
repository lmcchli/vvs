#if !defined(RTSAFE_ATOMIC_H)
#define RTSAFE_ATOMIC_H 1

#if !defined(WIN32)
/* Pretend we have included bits/atomicity.h */
#define _BITS_ATOMICITY_H 1
#include <atomic.h>
#define __exchange_and_add(x, y)  __rtsafe_exchange_and_add(x,y)
#define __atomic_add(x, y)  __rtsafe_atomic_add(x,y)

#if defined(__arch64__)

typedef long _Atomic_word;
#define __rtsafe_atomic_add(x,y) ::atomic_add_64((uint64_t*)x,y)
#define __rtsafe_exchange_and_add(x,y) ::atomic_add_64_nv((uint64_t*)x,y)

#else

typedef int _Atomic_word;
#define __rtsafe_atomic_add(x,y) ::atomic_add_32((uint32_t*)x,y)
#define __rtsafe_exchange_and_add(x,y) ::atomic_add_32_nv((uint32_t*)x,y)

#endif /* defined(__arch64__) */

#endif /* !defined(WIN32) */

#endif /* !defined(RTSAFE_ATOMIC_H) */
