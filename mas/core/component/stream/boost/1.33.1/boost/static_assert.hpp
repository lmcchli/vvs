//  (C) Copyright John Maddock 2000.
//  Use, modification and distribution are subject to the 
//  Boost Software License, Version 1.0. (See accompanying file 
//  LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

//  See http://www.boost.org/libs/static_assert for documentation.

/*
 Revision history:
   02 August 2000
      Initial version.
*/

#ifndef BOOST_STATIC_ASSERT_HPP
#define BOOST_STATIC_ASSERT_HPP

#include <boost/config.hpp>
#include <boost/detail/workaround.hpp>

#ifdef __BORLANDC__
//
// workaround for buggy integral-constant expression support:
#define BOOST_BUGGY_INTEGRAL_CONSTANT_EXPRESSIONS
#endif

#if defined(__GNUC__) && (__GNUC__ == 3) && ((__GNUC_MINOR__ == 3) || (__GNUC_MINOR__ == 4))
// gcc 3.3 and 3.4 don't produce good error messages with the default version:
#  define BOOST_SA_GCC_WORKAROUND
#endif

//
// If the compiler warns about unused typedefs then enable this:
//
#if defined(__GNUC__) && ((__GNUC__ > 4) || ((__GNUC__ == 4) && (__GNUC_MINOR__ >= 7)))
#  define BOOST_STATIC_ASSERT_UNUSED_ATTRIBUTE __attribute__((unused))
#else
#  define BOOST_STATIC_ASSERT_UNUSED_ATTRIBUTE
#endif

namespace boost{

// HP aCC cannot deal with missing names for template value parameters
template <bool x> struct STATIC_ASSERTION_FAILURE;

template <> struct STATIC_ASSERTION_FAILURE<true> { enum { value = 1 }; };

// HP aCC cannot deal with missing names for template value parameters
template<int x> struct static_assert_test{};

}

//
// Implicit instantiation requires that all member declarations be
// instantiated, but that the definitions are *not* instantiated.
//
// It's not particularly clear how this applies to enum's or typedefs;
// both are described as declarations [7.1.3] and [7.2] in the standard,
// however some compilers use "delayed evaluation" of one or more of
// these when implicitly instantiating templates.  We use typedef declarations
// by default, but try defining BOOST_USE_ENUM_STATIC_ASSERT if the enum
// version gets better results from your compiler...
//
// Implementation:
// Both of these versions rely on sizeof(incomplete_type) generating an error
// message containing the name of the incomplete type.  We use
// "STATIC_ASSERTION_FAILURE" as the type name here to generate
// an eye catching error message.  The result of the sizeof expression is either
// used as an enum initialiser, or as a template argument depending which version
// is in use...
// Note that the argument to the assert is explicitly cast to bool using old-
// style casts: too many compilers currently have problems with static_cast
// when used inside integral constant expressions.
//
#if !defined(BOOST_BUGGY_INTEGRAL_CONSTANT_EXPRESSIONS)

#if defined(BOOST_MSVC) && (BOOST_MSVC < 1300)
// __LINE__ macro broken when -ZI is used see Q199057
// fortunately MSVC ignores duplicate typedef's.
#define BOOST_STATIC_ASSERT( B ) \
   typedef ::boost::static_assert_test<\
      sizeof(::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >)\
      > boost_static_assert_typedef_
#elif defined(BOOST_MSVC)
#define BOOST_STATIC_ASSERT( B ) \
   typedef ::boost::static_assert_test<\
      sizeof(::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >)>\
         BOOST_JOIN(boost_static_assert_typedef_, __COUNTER__)
#elif defined(BOOST_INTEL_CXX_VERSION) || defined(BOOST_SA_GCC_WORKAROUND)
// agurt 15/sep/02: a special care is needed to force Intel C++ issue an error 
// instead of warning in case of failure
# define BOOST_STATIC_ASSERT( B ) \
    typedef char BOOST_JOIN(boost_static_assert_typedef_, __LINE__) \
        [ ::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >::value ]
#elif defined(__sgi)
// special version for SGI MIPSpro compiler
#define BOOST_STATIC_ASSERT( B ) \
   BOOST_STATIC_CONSTANT(bool, \
     BOOST_JOIN(boost_static_assert_test_, __LINE__) = ( B )); \
   typedef ::boost::static_assert_test<\
     sizeof(::boost::STATIC_ASSERTION_FAILURE< \
       BOOST_JOIN(boost_static_assert_test_, __LINE__) >)>\
         BOOST_JOIN(boost_static_assert_typedef_, __LINE__)
#elif BOOST_WORKAROUND(__MWERKS__, <= 0x3003)
// special version for CodeWarrior <= 8.x
#define BOOST_STATIC_ASSERT( B ) \
   BOOST_STATIC_CONSTANT(int, \
     BOOST_JOIN(boost_static_assert_test_, __LINE__) = \
       sizeof(::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >) )
#else
// generic version
#define BOOST_STATIC_ASSERT( B ) \
   typedef ::boost::static_assert_test<\
      sizeof(::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >)>\
         BOOST_JOIN(boost_static_assert_typedef_, __LINE__) BOOST_STATIC_ASSERT_UNUSED_ATTRIBUTE
#endif

#else
// alternative enum based implementation:
#define BOOST_STATIC_ASSERT( B ) \
   enum { BOOST_JOIN(boost_static_assert_enum_, __LINE__) \
      = sizeof(::boost::STATIC_ASSERTION_FAILURE< (bool)( B ) >) }
#endif


#endif // BOOST_STATIC_ASSERT_HPP


