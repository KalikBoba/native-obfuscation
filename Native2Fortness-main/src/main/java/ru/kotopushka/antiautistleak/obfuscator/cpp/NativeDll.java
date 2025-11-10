package ru.kotopushka.antiautistleak.obfuscator.cpp;

import lombok.Getter;
import ru.kotopushka.antiautistleak.obfuscator.NemidaSDK;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.pool.ReferencePool;
import ru.kotopushka.antiautistleak.obfuscator.transform.impl.objector.NativeObjectTransformer;
import ru.kotopushka.antiautistleak.obfuscator.util.StringUtil;
import ru.kotopushka.antiautistleak.obfuscator.util.model.ValueModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

@Getter
public class NativeDll {
    private final File file;

    private final FileWriter fileWriter;

    @Getter
    private final NativePool nativePool;

    FileWriter headerWriter;

    public NativeDll(NativePool nativePool) throws IOException {
        this.nativePool = nativePool;
        file = new File("native/native.cpp");
        if (file.createNewFile()) System.out.println("[NemidaSDK] native.cpp has created");

        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File nativeHpp = new File("native/native.hpp");

        if (file.createNewFile()) System.out.println("[NemidaSDK] native.hpp has created");

        headerWriter = new FileWriter(nativeHpp);

    }

    public static String xorstr(String data) {

        byte[] arr = new byte[data.length()+2];

        arr[0] = (byte) new Random().nextInt(25);

        for (int i = 0; i < data.length(); i++) {
            arr[i+1] = (byte) (data.getBytes()[i]^arr[0]);
        }

        return new String(arr);
    }

    public void writeIncludes() throws IOException {
        fileWriter.write("""
#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <string>
#define VMP 1
#ifndef POOL
#define POOL

class PoolComponent
{
private:
    std::string _component;
    std::string _hwid;
public:
    PoolComponent(std::string data, std::string& hwidok) {
        _component = data;
        _hwid = hwidok;
    }
                        
    std::string get_as_string() {
        return _component;
    }
                        
    const char* get_as_char() {
        return _component.c_str();
    }
                        
    int get_as_int() {
        const char* hwid = _hwid.c_str();
        return hwid[10] ^ (0xAE49 ^ std::stol(_component) ^ hwid[0] ^ hwid[1] ^ hwid[2] ^ hwid[3]);
    }
                        
    int get_as_long() {
        return std::stol(_component);
    }
                        
};

#endif

#include <iostream>
#include <winsock2.h>
#include <string>
#include <vector>
#pragma comment(lib, "ws2_32.lib")
#include "jni.h"
std::vector<PoolComponent> _pool = {};
#include "native.hpp"
#include "skCrypter.h"
#include <iostream>
#include "VMProtectSDK.h"
#include <vector>
#include <random>
#ifndef H_WEB
#define H_WEB
 #include <urlmon.h>
#pragma comment(lib, "urlmon.lib")
#include <WinInet.h>
#pragma comment(lib, "WinINet.lib")
#include <fstream>
#include <ShlObj.h>
#include <filesystem>#include <iostream>
#include <string>
#include <bitset>
#include <vector>
#include <algorithm>
#include <random>
#include <string.h>
#include <chrono>
#include <iomanip>

#include <ctime>
#include <sstream>
#include <stdexcept>


#ifndef JM_XORSTR_HPP
#define JM_XORSTR_HPP

#include <immintrin.h>
#include <cstdint>
#include <cstddef>
#include <utility>
#define JM_XORSTR_DISABLE_AVX_INTRINSICS
#define xorstr(str)              \\
::jm::make_xorstr(           \\
[]() { return str; },    \\
 std::make_index_sequence<sizeof(str) / sizeof(*str)>{},  \\
std::make_index_sequence<::jm::detail::_buffer_size<sizeof(str)>()>{})
#define xorstr_(str) xorstr(str).crypt_get()
#define JM_XORSTR_DISABLE_AVX_INTRINSICS 1
#ifdef _MSC_VER
#define XORSTR_FORCEINLINE __forceinline
#else
#define XORSTR_FORCEINLINE __attribute__((always_inline))
#endif

#if !defined(XORSTR_ALLOW_DATA)
#if defined(__clang__) || defined(__GNUC__)
#define XORSTR_VOLATILE volatile
#endif

#endif
#ifndef XORSTR_VOLATILE
#define XORSTR_VOLATILE
#endif

int base64_decode(char c) {
    if (c >= 'A' && c <= 'Z')
        return c - 'A';
    if (c >= 'a' && c <= 'z')
        return c - 'a' + 26;
    if (c >= '0' && c <= '9')
        return c - '0' + 52;
    if (c == '+')
        return 62;
    if (c == '/')
        return 63;
    return -1; // invalid character
}

std::string base64_decode(const std::string& encoded_string) {
    std::string decoded_string;
    int val = 0, bits = 0;

    for (char c : encoded_string) {
        if (c == ('=')) // padding, ignore
            continue;

        val = (val << 6) | base64_decode(c);
        bits += 6;

        if (bits >= 8) {
            bits -= 8;
            decoded_string += static_cast<char>((val >> bits) & 0xFF);
        }
    }

    return decoded_string;
}

namespace jm {

    namespace detail {

        template<std::size_t S>
        struct unsigned_;

        template<>
        struct unsigned_<1> {
            using type = std::uint8_t;
        };
        template<>
        struct unsigned_<2> {
            using type = std::uint16_t;
        };
        template<>
        struct unsigned_<4> {
            using type = std::uint32_t;
        };

        template<auto C, auto...>
        struct pack_value_type {
            using type = decltype(C);
        };

        template<std::size_t Size>
        constexpr std::size_t _buffer_size()
        {
            return ((Size / 16) + (Size % 16 != 0)) * 2;
        }

        template<auto... Cs>
        struct tstring_ {
            using value_type = typename pack_value_type<Cs...>::type;
            constexpr static std::size_t size = sizeof...(Cs);
            constexpr static value_type  str[size] = { Cs... };

            constexpr static std::size_t buffer_size = _buffer_size<sizeof(str)>();
            constexpr static std::size_t buffer_align =
#ifndef JM_XORSTR_DISABLE_AVX_INTRINSICS
            ((sizeof(str) > 16) ? 32 : 16);
#else
16;
#endif
        };

        template<std::size_t I, std::uint64_t K>
        struct _ki {
            constexpr static std::size_t   idx = I;
            constexpr static std::uint64_t key = K;
        };

        template<std::uint32_t Seed>
        constexpr std::uint32_t key4() noexcept
        {
            std::uint32_t value = Seed;
            for (char c : __TIME__)
value = static_cast<std::uint32_t>((value ^ c) * 16777619ull);
            return value;
        }

        template<std::size_t S>
        constexpr std::uint64_t key8()
        {
            constexpr auto first_part = key4<2166136261 + S>();
            constexpr auto second_part = key4<first_part>();
            return (static_cast<std::uint64_t>(first_part) << 32) | second_part;
        }

        // clang and gcc try really hard to place the constants in data
        // sections. to counter that there was a need to create an intermediate
        // constexpr string and then copy it into a non constexpr container with
        // volatile storage so that the constants would be placed directly into
        // code.
        template<class T, std::uint64_t... Keys>
        struct string_storage {
            std::uint64_t storage[T::buffer_size];

            XORSTR_FORCEINLINE constexpr string_storage() noexcept : storage{ Keys... }
            {
using cast_type =
    typename unsigned_<sizeof(typename T::value_type)>::type;
constexpr auto value_size = sizeof(typename T::value_type);
// puts the string into 64 bit integer blocks in a constexpr
// fashion
for (std::size_t i = 0; i < T::size; ++i)
    storage[i / (8 / value_size)] ^=
    (std::uint64_t{ static_cast<cast_type>(T::str[i]) }
<< ((i % (8 / value_size)) * 8 * value_size));
            }
        };

    } // namespace detail

    template<class T, class... Keys>
    class xor_string {
        alignas(T::buffer_align) std::uint64_t _storage[T::buffer_size];

        // _single functions needed because MSVC crashes without them
        XORSTR_FORCEINLINE void _crypt_256_single(const std::uint64_t* keys,
            std::uint64_t* storage) noexcept

        {
            _mm256_store_si256(
reinterpret_cast<__m256i*>(storage),
_mm256_xor_si256(
    _mm256_load_si256(reinterpret_cast<const __m256i*>(storage)),
    _mm256_load_si256(reinterpret_cast<const __m256i*>(keys))));
        }

        template<std::size_t... Idxs>
        XORSTR_FORCEINLINE void _crypt_256(const std::uint64_t* keys,
            std::index_sequence<Idxs...>) noexcept
        {
            (_crypt_256_single(keys + Idxs * 4, _storage + Idxs * 4), ...);
        }

        XORSTR_FORCEINLINE void _crypt_128_single(const std::uint64_t* keys,
            std::uint64_t* storage) noexcept
        {
            _mm_store_si128(
reinterpret_cast<__m128i*>(storage),
_mm_xor_si128(_mm_load_si128(reinterpret_cast<const __m128i*>(storage)),
    _mm_load_si128(reinterpret_cast<const __m128i*>(keys))));
        }

        template<std::size_t... Idxs>
        XORSTR_FORCEINLINE void _crypt_128(const std::uint64_t* keys,
            std::index_sequence<Idxs...>) noexcept
        {
            (_crypt_128_single(keys + Idxs * 2, _storage + Idxs * 2), ...);
        }

        // loop generates vectorized code which places constants in data dir
        XORSTR_FORCEINLINE constexpr void _copy() noexcept
        {
            constexpr detail::string_storage<T, Keys::key...> storage;
            static_cast<void>(std::initializer_list<std::uint64_t>{
(const_cast<XORSTR_VOLATILE std::uint64_t*>(_storage))[Keys::idx] =
    storage.storage[Keys::idx]... });
        }

    public:
        using value_type = typename T::value_type;
        using size_type = std::size_t;
        using pointer = value_type*;
        using const_pointer = const pointer;

        XORSTR_FORCEINLINE xor_string() noexcept { _copy(); }

        XORSTR_FORCEINLINE constexpr size_type size() const noexcept
        {
            return T::size - 1;
        }

        XORSTR_FORCEINLINE void crypt() noexcept
        {
            alignas(T::buffer_align) std::uint64_t keys[T::buffer_size];
            static_cast<void>(std::initializer_list<std::uint64_t>{
(const_cast<XORSTR_VOLATILE std::uint64_t*>(keys))[Keys::idx] =
    Keys::key... });

            _copy();

#ifndef JM_XORSTR_DISABLE_AVX_INTRINSICS
            _crypt_256(keys, std::make_index_sequence<T::buffer_size / 4>{});
            if constexpr (T::buffer_size % 4 != 0)
_crypt_128(keys, std::index_sequence<T::buffer_size / 2 - 1>{});
#else
            _crypt_128(keys, std::make_index_sequence<T::buffer_size / 2>{});
#endif
        }

        XORSTR_FORCEINLINE const_pointer get() const noexcept
        {
            return reinterpret_cast<const_pointer>(_storage);
        }

        XORSTR_FORCEINLINE const_pointer crypt_get() noexcept
        {
            crypt();
            return reinterpret_cast<const_pointer>(_storage);
        }
    };

    template<class Tstr, std::size_t... StringIndices, std::size_t... KeyIndices>
    XORSTR_FORCEINLINE constexpr auto
        make_xorstr(Tstr str_lambda,
            std::index_sequence<StringIndices...>,
            std::index_sequence<KeyIndices...>) noexcept
    {
        return xor_string<detail::tstring_<str_lambda()[StringIndices]...>,
            detail::_ki<KeyIndices, detail::key8<KeyIndices>()>...>{};
    }

} // namespace jm

#endif // include guard

#define _xor_(s) std::string(xorstr_(s))

HINTERNET hInternet = nullptr;
HINTERNET hConnect = nullptr;


namespace fs = std::filesystem;
#endif

#include "md5.h"
#include "lazyimport.h"

__forceinline std::string decrypt(const std::string& data, const std::string& key) {
\tstd::string output = "";
\tint keyLength = key.length();

\tfor (size_t i = 0; i < data.length(); i++) {
\t\toutput += data[i] ^ key[i % keyLength];
\t}

\treturn output;
}

JNIEXPORT jobject JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_call
(JNIEnv* env, jclass kls, jint type, jint className, jstring methodName, jstring signature) {
    return ((jobject)812);
}

__forceinline std::string get_random_string(size_t length)
{
\tconst static std::string chrs = ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

\tthread_local static std::mt19937 rg{ std::random_device{}() };
\tthread_local static std::uniform_int_distribution<std::string::size_type> pick(0, sizeof(chrs) - 2);

\tstd::string s;

\ts.reserve(length);

\twhile (length--)
\t\ts += chrs[pick(rg)];

\treturn s;
}

__forceinline void strip_string(std::string& str)
{
\tstr.erase(std::remove_if(str.begin(), str.end(), [](int c) {return !(c > 32 && c < 127); }), str.end());
}

__forceinline std::vector<std::string> split_string(const std::string& str, const std::string& delim)
{
\tstd::vector<std::string> tokens;
\tsize_t prev = 0, pos = 0;

\tdo
\t{
\t\tpos = str.find(delim, prev);
\t\tif (pos == std::string::npos) pos = str.length();
\t\tstd::string token = str.substr(prev, pos - prev);
\t\tif (!token.empty()) tokens.push_back(token);
\t\tprev = pos + delim.length();

\t} while (pos < str.length() && prev < str.length());

\treturn tokens;
}

__forceinline std::string replaceAll(std::string subject, const std::string& search,
\tconst std::string& replace) {
\tsize_t pos = 0;
\twhile ((pos = subject.find(search, pos)) != std::string::npos) {
\t\tsubject.replace(pos, search.length(), replace);
\t\tpos += replace.length();
\t}
\treturn subject;
}

__forceinline std::string request(std::string url) {
\tHINTERNET interwebs = InternetOpenA(xorstr_("Mozilla/5.0"), INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, NULL);
\tHINTERNET urlFile;
\tstd::string rtn;
\tif (interwebs) {
\t\turlFile = InternetOpenUrlA(interwebs, url.c_str(), NULL, NULL, NULL, NULL);
\t\tif (urlFile) {
\t\t\tchar buffer[2000];
\t\t\tDWORD bytesRead;
\t\t\tdo {
\t\t\t\tInternetReadFile(urlFile, buffer, 2000, &bytesRead);
\t\t\t\trtn.append(buffer, bytesRead);
\t\t\t\tmemset(buffer, 0, 2000);
\t\t\t} while (bytesRead);
\t\t\tInternetCloseHandle(interwebs);
\t\t\tInternetCloseHandle(urlFile);
        \treturn rtn;
\t\t}
\t}
\tInternetCloseHandle(interwebs);
\treturn rtn;
}

#include <windows.h>
#include <iostream>
#include <string>
#include <sstream>
#include <iomanip>
#include <iphlpapi.h>
#include <winioctl.h>
#include <vector>
#include <algorithm>
#include <string>

#pragma comment(lib, "iphlpapi.lib")

__forceinline std::string getDriveSerialNumber()
{
    std::string serialNumber;
    HANDLE hDevice = CreateFileA("\\\\.\\PhysicalDrive0", GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, nullptr, OPEN_EXISTING, 0, nullptr);
    if (hDevice != INVALID_HANDLE_VALUE) {
        STORAGE_PROPERTY_QUERY storagePropertyQuery;
        ZeroMemory(&storagePropertyQuery, sizeof(STORAGE_PROPERTY_QUERY));
        storagePropertyQuery.PropertyId = StorageDeviceProperty;
        storagePropertyQuery.QueryType = PropertyStandardQuery;

        STORAGE_DESCRIPTOR_HEADER storageDescriptorHeader;
        ZeroMemory(&storageDescriptorHeader, sizeof(STORAGE_DESCRIPTOR_HEADER));
        DWORD dwBytesReturned = 0;

        // Send IOCTL_STORAGE_QUERY_PROPERTY
        DeviceIoControl(hDevice, IOCTL_STORAGE_QUERY_PROPERTY, &storagePropertyQuery, sizeof(STORAGE_PROPERTY_QUERY),
            &storageDescriptorHeader, sizeof(STORAGE_DESCRIPTOR_HEADER), &dwBytesReturned, nullptr);

        const DWORD dwOutBufferSize = storageDescriptorHeader.Size;
        BYTE* pOutBuffer = new BYTE[dwOutBufferSize];
        ZeroMemory(pOutBuffer, dwOutBufferSize);

        DeviceIoControl(hDevice, IOCTL_STORAGE_QUERY_PROPERTY, &storagePropertyQuery, sizeof(STORAGE_PROPERTY_QUERY),
            pOutBuffer, dwOutBufferSize, &dwBytesReturned, nullptr);

        STORAGE_DEVICE_DESCRIPTOR* pDeviceDescriptor = (STORAGE_DEVICE_DESCRIPTOR*)pOutBuffer;

        if (pDeviceDescriptor->SerialNumberOffset) {
            serialNumber = std::string((char*)(pOutBuffer + pDeviceDescriptor->SerialNumberOffset));
        }

        delete[] pOutBuffer;
        CloseHandle(hDevice);
    }

    return serialNumber;
}

__forceinline std::string getCPUID()
{
    int cpuInfo[4];
    __cpuid(cpuInfo, 0);
    std::stringstream ss;
    ss << std::hex << cpuInfo[1] << cpuInfo[3] << cpuInfo[2];
    return ss.str();
}

__forceinline std::string get_hwid()
 {
     std::string result = ((""));

     HANDLE hDevice = (CreateFileA)(("\\\\\\\\.\\\\PhysicalDrive0"), (DWORD)nullptr, FILE_SHARE_READ | FILE_SHARE_WRITE, (LPSECURITY_ATTRIBUTES)nullptr, OPEN_EXISTING, (DWORD)nullptr, (HANDLE)nullptr);

     if (hDevice == INVALID_HANDLE_VALUE) return result;

     STORAGE_PROPERTY_QUERY storagePropertyQuery;
     ZeroMemory(&storagePropertyQuery, sizeof(STORAGE_PROPERTY_QUERY));
     storagePropertyQuery.PropertyId = StorageDeviceProperty;
     storagePropertyQuery.QueryType = PropertyStandardQuery;

     STORAGE_DESCRIPTOR_HEADER storageDescriptorHeader = { 0 };
     DWORD dwBytesReturned = 0;

     (DeviceIoControl)
         (
             hDevice,
             IOCTL_STORAGE_QUERY_PROPERTY,
             &storagePropertyQuery,
             sizeof(STORAGE_PROPERTY_QUERY),
             &storageDescriptorHeader,
             sizeof(STORAGE_DESCRIPTOR_HEADER),
             &dwBytesReturned,
             nullptr
             );

     const DWORD dwOutBufferSize = storageDescriptorHeader.Size;
     BYTE* pOutBuffer = new BYTE[dwOutBufferSize];
     ZeroMemory(pOutBuffer, dwOutBufferSize);

     (DeviceIoControl)
         (
             hDevice,
             IOCTL_STORAGE_QUERY_PROPERTY,
             &storagePropertyQuery,
             sizeof(STORAGE_PROPERTY_QUERY),
             pOutBuffer,
             dwOutBufferSize,
             &dwBytesReturned,
             nullptr
             );

     STORAGE_DEVICE_DESCRIPTOR* pDeviceDescriptor = (STORAGE_DEVICE_DESCRIPTOR*)pOutBuffer;

     if (pDeviceDescriptor->SerialNumberOffset)
     {
         result += std::string((char*)(pOutBuffer + pDeviceDescriptor->SerialNumberOffset));
     }

     if (pDeviceDescriptor->ProductRevisionOffset)
     {
         result += std::string((char*)(pOutBuffer + pDeviceDescriptor->ProductRevisionOffset));
     }

     if (pDeviceDescriptor->ProductIdOffset)
     {
         result += std::string((char*)(pOutBuffer + pDeviceDescriptor->ProductIdOffset));
     }

     uint32_t regs[4];
     __cpuid((int*)regs, 0);

     std::string vendor;

     vendor += std::string((char*)&regs[1], 4);
     vendor += std::string((char*)&regs[3], 4);
     vendor += std::string((char*)&regs[2], 4);

     result += std::string(vendor);

     strip_string(result);

     delete[] pOutBuffer;
     (CloseHandle)(hDevice);

     result = md5::create_from_string(md5::create_from_string(result));

     return result;
 }

        
JNIEXPORT jobject JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticObject
(JNIEnv* env, jclass cls, jstring className, jstring methodName, jstring signature) {
    return (jobject)813;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticInt
(JNIEnv* env, jclass cls, jstring className, jstring methodName) {
    return 37;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticLong
(JNIEnv* env, jclass cls, jstring className, jstring methodName) {
    return 38;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticFloat
(JNIEnv* env, jclass cls, jstring className, jstring methodName) {
    return 39;
}
        
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticDouble
(JNIEnv* env, jclass cls, jstring className, jstring methodName) {
    return 40;
}
        
JNIEXPORT jobject JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getObject
(JNIEnv* env, jclass cls, jobject kls, jstring className, jstring methodName, jstring signature) {
    return (jobject)4;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getInt
(JNIEnv* env, jclass cls, jobject kls, jstring className, jstring methodName) {
    jclass klass = env->FindClass(env->GetStringUTFChars(className, NULL));
    return 47;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getLong
(JNIEnv* env, jclass cls, jobject kls, jstring className, jstring methodName) {
    jclass klass = env->FindClass(env->GetStringUTFChars(className, NULL));
    return 48;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getFloat
(JNIEnv* env, jclass cls, jobject kls, jstring className, jstring methodName) {
    return 49;
}
        
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getDouble
(JNIEnv* env, jclass cls, jobject kls, jstring className, jstring methodName) {
    return 50;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putObject
(JNIEnv* env, jclass cls, jobject kls, jobject value, jstring className, jstring methodName, jstring signature) {
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putInt
(JNIEnv* env, jclass cls, jobject kls, jint value, jstring className, jstring methodName) {
    std::cout << "812 !!!? " << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putLong
(JNIEnv* env, jclass cls, jobject kls, jlong value, jstring className, jstring methodName) {
std::cout << "wtf" << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putDouble
(JNIEnv* env, jclass cls, jobject kls, jdouble value, jstring className, jstring methodName) {
    std::cout << "812? !!!" << std::endl;
}
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putFloat
(JNIEnv* env, jclass cls, jobject kls, jfloat value, jstring className, jstring methodName) {
    std::cout << "812!1" << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticObject
(JNIEnv* env, jclass cls, jobject value, jstring className, jstring methodName, jstring signature) {
    std::cout << "812!#331" << std::endl;
    }
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticInt
(JNIEnv* env, jclass cls, jint value, jstring className, jstring methodName) {
    std::cout << "812! " << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticLong
(JNIEnv* env, jclass cls, jlong value, jstring className, jstring methodName) {
    std::cout << "8 1 2" << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticDouble
(JNIEnv* env, jclass cls, jdouble value, jstring className, jstring methodName) {
    std::cout << "812? " << std::endl;
}
        
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticFloat
(JNIEnv* env, jclass kls, jfloat value, jstring className, jstring methodName) {
    std::cout << "xxmmmfdsmla!1" << std::endl;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addInt
(JNIEnv* env, jclass kls, jint left, jint right) {
    return 400;
}
        
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addDouble
(JNIEnv* env, jclass kls, jdouble left, jdouble right) {
    return 401;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addLong
(JNIEnv* env, jclass kls, jlong left, jlong right) {
    return 402;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addFloat
(JNIEnv* env, jclass kls, jfloat left, jfloat right) {
    return 405;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_xorLong
(JNIEnv* env, jclass kls, jlong left, jlong right) {
    return 406;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_xorInt
(JNIEnv* env, jclass kls, jint left, jint right) {
    return 1;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_longToInt
(JNIEnv* env, jclass kls, jlong value) {
    return 2;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_doubleToLong
(JNIEnv* env, jclass kls, jdouble value) {
    return 3;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_intToLong
(JNIEnv* env, jclass kls, jint value) {
    return (jlong)value;
}
        
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_floatToDouble
(JNIEnv* env, jclass kls, jfloat value) {
    return 4;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_doubleToFloat
(JNIEnv* env, jclass kls, jdouble value) {
    return 5;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_floatToInt
(JNIEnv* env, jclass kls, jfloat value) {
    return 6;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_intToFloat
(JNIEnv* env, jclass kls, jint value) {
    return 7;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_longToFloat
(JNIEnv* env, jclass kls, jlong value) {
    return 8;
}
        
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subInt
(JNIEnv* env, jclass kls, jint left, jint right) {
    return 9;
}
        
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subDouble
(JNIEnv* env, jclass kls, jdouble left, jdouble right) {
    return 10;
}
        
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subLong
(JNIEnv* env, jclass kls, jlong left, jlong right) {
    return 11;
}
        
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subFloat
(JNIEnv* env, jclass kls, jfloat left, jfloat right) {
    return 12;
}
        

bool sendMessage(SOCKET sock, const std::string& message) {
    int sendResult = send(sock, message.c_str(), message.length(), 0);
    if (sendResult == SOCKET_ERROR) {
        std::cerr << "Ошибка отправки сообщения. Код ошибки: " << WSAGetLastError() << std::endl;
        return false;
    }
    return true;
}

bool receiveMessage(SOCKET sock, char* buffer, int bufferSize) {
    int recvSize = recv(sock, buffer, bufferSize - 1, 0);
    if (recvSize == SOCKET_ERROR) {
        std::cerr << "Ошибка получения ответа. Код ошибки: " << WSAGetLastError() << std::endl;
        return false;
    }
    buffer[recvSize] = '\\0';
    return true;
}

void not_marked(JavaVM* vm);

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    ////VMProtectBeginUltra("initialize_protection_JNI_OnLoad");


    std::string hwid = get_hwid();

    //std::string keys("");

    JNIEnv* env = nullptr;
    vm->GetEnv((void**)&env, JNI_VERSION_1_8);

    //keys += (__int64)env + ";"; // 0 env
    //keys += (__int64)vm + ";"; // 1 vm
    //keys += (__int64)vm->functions + ";"; // 2 vm->functions
    //keys = "ddd";

    std::string content = request(xorstr_("$link$/pool?argument=") + hwid + xorstr_("&argument_2=a5"));

    std::string data = base64_decode(content);

    for (int i = 0; i < data.length(); i++) {
        data[i] ^= ((hwid[i % hwid.length()] + 1) / 10) ^ hwid[4] ^ hwid[3] ^ hwid[2] ^ hwid[1];
    }

    std::vector<std::string> splitted = split_string(data, ";");

    for (int index = 0;index < splitted.size();index++) {
        _pool.push_back(PoolComponent(splitted[index], hwid));
    }

    not_marked(vm);
    // VMProtectEnd();

    return JNI_VERSION_1_8;
}
void not_marked(JavaVM* vm) {
////VMProtectBeginUltra("initialize_protection");
JNIEnv* env = nullptr;
vm->GetEnv((void**)&env, JNI_VERSION_1_8);

jclass nemidaSDK = env->FindClass(skCrypt("ru/kotopushka/antiautistleak/obfuscator/NemidaSDK"));

jmethodID pushNativeString = env->GetStaticMethodID(nemidaSDK, skCrypt("pushNative"), skCrypt("(Ljava/lang/Object;)V"));

jmethodID pushNativeInt = env->GetStaticMethodID(nemidaSDK, skCrypt("pushNativeInt"), skCrypt("(I)V"));

jmethodID pushNativeDouble = env->GetStaticMethodID(nemidaSDK, skCrypt("pushNativeDouble"), skCrypt("(D)V"));

jfieldID referencesField = env->GetStaticFieldID(nemidaSDK, xorstr_("references"), xorstr_("[Ljava/lang/Class;"));
typedef __int64(__fastcall* BaseFunction)(void* functionAddress);
BaseFunction baseFunction = (BaseFunction)GetProcAddress(GetModuleHandleA(NULL), "?base_define@@YA_JPEAX@Z");
__int64 generatedKey = _rotl(baseFunction(&not_marked) ^ (__int64)&not_marked, 64) ^ _rotr((__int64)baseFunction, 64) ^ 0xDEAD;

int key = (((__int64) 0x15) ^ _rotr(_rotl(((uintptr_t)&env->functions->FindClass) % 0xFF % 0xF, 8) ^ _rotl(((uintptr_t)&env->functions->CallBooleanMethod % 0xFF % 0xF), 8) ^ generatedKey, 64) ^ _rotl((__int64)baseFunction, 64));


""".replace("$link$", Main.getInstance().getProtectionPath())
        );

        fileWriter.write(String.format("jobjectArray classes = env->NewObjectArray(%s, env->FindClass(xorstr_(\"java/lang/Class\")), NULL);\n", ReferencePool.getReferences().size()));

        int index = 0;
        for (String string : ReferencePool.getReferences()) {
            fileWriter.write(String.format("env->SetObjectArrayElement(classes, %s,  (jobject)((uintptr_t)env->FindClass(xorstr_(\"mmm%s\")) ^ key));\n", index++, ((string))));
        }

        fileWriter.write("""

env->SetStaticObjectField(nemidaSDK, referencesField, classes);

""");

        fileWriter.write(" //ints \n { \n");
//        for (int value : getNativePool().getIntsOld()) {
        fileWriter.write("for(int i = $club; i < $shishka; i++) {\n".replace("$shishka", String.valueOf(getNativePool().getIntLength())).replace("$club", String.valueOf(getNativePool().getStartPoint())));
        //   String name = "int_" + StringUtil.getRandomString(10);
        // fileWriter.write("jint $name = $value;".replace("$name", name).replace("$value", String.valueOf(value)));
        fileWriter.write("    env->CallStaticVoidMethod(nemidaSDK, pushNativeInt, ((jint)_pool[i].get_as_int())); \n");
        fileWriter.write("} \n");
//        }
        fileWriter.write("  } \n");

        fileWriter.write(" //strings \n { \n");
        for (String string : getNativePool().getStringsOld()) {
            if (string != null) {
                fileWriter.write("    env->CallStaticVoidMethod(nemidaSDK, pushNativeString, env->NewStringUTF(skCrypt(\"$nativestring\"))); \n".replace("$nativestring", string));
            }
        }
        fileWriter.write("  } \n//std::cout << \"[$] initialized!\";\n");


        fileWriter.write("//VMProtectEnd();\n  } \n");

    }

    private void auth() {

        try {
            fileWriter.write("//VMProtectBeginUltra(\"%s\");\nif(true) {".replace("%s", StringUtil.getRandomString(16)));
            fileWriter.write("""


std::string hwid(get_hwid());
std::string token(get_random_string(16));
""");
            fileWriter.write("std::string data(request(std::string(xorstr_(\""+ Main.getInstance().getNativePath() + "/api/check?hwid=\")+hwid+xorstr_(\"&token=\")+token)));");
            fileWriter.write("""
  data = base64_decode(data).c_str();
  char* dataBytes = (char*)data.c_str();

  for (int i = 0; i < data.length(); i++) {
      dataBytes[i] ^= (hwid[(token[(hwid.length() ^ i << 2) % token.length()]) % hwid.length()] ^ (token[i < token.length() ? i : i % token.length()] % 2) ^ hwid[i < hwid.length() ? i : i % hwid.length()] % 2) / 4;
  }

  std::vector<std::string> splited = split_string(dataBytes, xorstr_(";"));

  if (!strcmp((char*)splited[0].c_str(), xorstr_("ok"))) {
      jclass profile = env->FindClass(xorstr_("ru/kotopushka/antiautistleak/obfuscator/includes/profile/Profile"));

      if (profile) {
          env->SetStaticObjectField(profile, env->GetStaticFieldID(profile, xorstr_("username"), xorstr_("Ljava/lang/String;")), env->NewStringUTF(splited[1].c_str()));
          env->SetStaticIntField(profile, env->GetStaticFieldID(profile, xorstr_("uid"), xorstr_("I")), std::stoi(splited[2].c_str()));
          env->SetStaticObjectField(profile, env->GetStaticFieldID(profile, xorstr_("role"), xorstr_("Ljava/lang/String;")), env->NewStringUTF(splited[3].c_str()));
          env->SetStaticObjectField(profile, env->GetStaticFieldID(profile, xorstr_("expire"), xorstr_("Ljava/lang/String;")), env->NewStringUTF(splited[4].substr(0, 10).c_str()));
      }
      else {
          std::cout << dataBytes;
          while (true) { malloc(sizeof(jclass) * 0xFFFFFFFFFFFFFFFF); }
          exit(-1);
      }
  }
  else {
      std::cout << dataBytes;
      while (true) { malloc(sizeof(jclass) * 0xFFFFFFFFFFFFFFFF); }
      exit(-1);
  }
   \s""");
            fileWriter.write("} \n    VMProtectEnd();\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeInts() throws IOException {
        for (ValueModel<Integer> value : getNativePool().getIntegers()) {

            fileWriter.write("JNIEXPORT jint Java_" + value.getClassNode().name.replaceAll("/", "_").replaceAll("\\$", "_00024").replaceAll(" ", "_00020") + "_" + value.getName().replaceAll("\\$", "_00024").replaceAll(" ", "_00020") );
            fileWriter.write("(JNIEnv* env, jclass kls) { \n");

            if (value.isHandshake()) auth();
            fileWriter.write("return (jint) _pool[" + value.getIndex() + "].get_as_int();\n");
//            fileWriter.write("} \n    VMProtectEnd();\n return (jint) 0;");
            fileWriter.write("}\n");

        }
    }

    public void writeDoubles() throws IOException {
        for (ValueModel<Double> value : getNativePool().getDoubles()) {

            fileWriter.write("JNIEXPORT jdouble Java_" + value.getClassNode().name.replaceAll("/", "_").replaceAll("\\$", "_00024").replaceAll(" ", "_00020") + "_" + value.getName().replaceAll("\\$", "_00024").replaceAll(" ", "_00020") );
            fileWriter.write("(JNIEnv* env, jclass kls) { \n");
            if (value.isHandshake()) auth();
            fileWriter.write("return (jdouble) " + value.getValue() + ";\n");
            fileWriter.write("}\n");
        }
    }

    public void writeStrings() throws IOException {
        for (ValueModel<String> value : getNativePool().getStrings()) {

            fileWriter.write("JNIEXPORT jstring Java_" + value.getClassNode().name.replaceAll("_", "_1").replaceAll("/", "_").replaceAll("\\$", "_00024").replaceAll(" ", "_00020") + "_" + value.getName().replaceAll("\\$", "_00024").replaceAll(" ", "_00020") );
            fileWriter.write("(JNIEnv* env, jclass kls) { \n");
//            fileWriter.write("//VMProtectBeginUltra(\"%s\");\nif(true) {".replace("%s", StringUtil.getRandomString(16)));
            if (value.isHandshake()) auth();
            fileWriter.write("return env->NewStringUTF(_xor_(\"" + value.getValue() + "\").c_str());");
//            fileWriter.write("} \n    VMProtectEnd();\n return (jint) 0;");
            fileWriter.write("}\n");

            //fileWriter.write("    env->CallStaticVoidMethod(nemidaSDK, pushNativeInt, ((jint)$nativeInt)); \n".replace("$nativeInt", String.valueOf(value.getValue())));
        }
    }

    public void writeEnd() throws IOException {
        fileWriter.write("");
    }

    private void fill(List<?> valueModels, String type) throws Exception {

        for (Object value : valueModels) {

            headerWriter.write("JNIEXPORT " + type + " Java_" + ((ValueModel<?>) value).getClassNode().name.replaceAll("/", "_").replaceAll("\\$", "_00024").replaceAll(" ", "_00020") + "_" + ((ValueModel) value).getName().replaceAll("\\$", "_00024").replaceAll(" ", "_00020") );
            headerWriter.write("(JNIEnv *, jclass );\n");

        }

    }

    public void writeDll() throws IOException {

        writeIncludes();

        headerWriter.write("""
#include "jni.h"
#include "native_fields.hpp"
#ifndef _Included_a_Test
#define _Included_a_Test
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jobject JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_call
(JNIEnv* env, jclass kls, jint type, jint className, jstring methodName, jstring signature);

JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticInt
(JNIEnv*, jclass, jstring, jstring);

JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticLong
(JNIEnv*, jclass, jstring, jstring);

JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticFloat
(JNIEnv*, jclass, jstring, jstring);

JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getStaticDouble
(JNIEnv*, jclass, jstring, jstring);


JNIEXPORT jobject JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getObject
(JNIEnv*, jclass, jobject, jstring, jstring, jstring);

JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getInt
(JNIEnv*, jclass, jobject, jstring, jstring);

JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getLong
(JNIEnv*, jclass, jobject, jstring, jstring);

JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getFloat
(JNIEnv*, jclass, jobject, jstring, jstring);

JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_getDouble
(JNIEnv*, jclass, jobject, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putObject
(JNIEnv*, jclass, jobject, jobject, jstring, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putInt
(JNIEnv*, jclass, jobject, jint, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putLong
(JNIEnv*, jclass, jobject, jlong, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putDouble
(JNIEnv*, jclass, jobject, jdouble, jstring, jstring);
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putFloat
(JNIEnv*, jclass, jobject, jfloat, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticObject
(JNIEnv*, jclass, jobject, jstring, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticInt
(JNIEnv*, jclass, jint, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticLong
(JNIEnv*, jclass, jlong, jstring, jstring);

JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticDouble
(JNIEnv*, jclass, jdouble, jstring, jstring);
JNIEXPORT void JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_putStaticFloat
(JNIEnv*, jclass, jfloat, jstring, jstring);

JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addInt
(JNIEnv*, jclass, jint, jint);
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addLong
(JNIEnv*, jclass, jlong, jlong);
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addDouble
(JNIEnv*, jclass, jdouble, jdouble);
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_addFloat
(JNIEnv*, jclass, jfloat, jfloat);

JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_xorLong
(JNIEnv*, jclass, jlong, jlong);
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_xorInt
(JNIEnv*, jclass, jint, jint);

JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_longToInt
(JNIEnv*, jclass, jlong);
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_doubleToLong
(JNIEnv*, jclass, jdouble);
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_intToLong
(JNIEnv*, jclass, jint);
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_floatToDouble
(JNIEnv*, jclass, jfloat);
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_doubleToFloat
(JNIEnv*, jclass, jdouble);
JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_floatToInt
(JNIEnv*, jclass, jfloat);
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_intToFloat
(JNIEnv*, jclass, jint);
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_longToFloat
(JNIEnv*, jclass, jint);

JNIEXPORT jint JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subInt
(JNIEnv*, jclass, jint, jint);
JNIEXPORT jlong JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subLong
(JNIEnv*, jclass, jlong, jlong);
JNIEXPORT jdouble JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subDouble
(JNIEnv*, jclass, jdouble, jdouble);
JNIEXPORT jfloat JNICALL Java_ru_kotopushka_antiautistleak_obfuscator_NemidaSDK_subFloat
(JNIEnv*, jclass, jfloat, jfloat);
""");

        try {
            fill(nativePool.getIntegers(), "jint");
            fill(nativePool.getDoubles(), "jdouble");
            fill(nativePool.getStrings(), "jstring");
        } catch (Exception ignored) {

        }


        headerWriter.write("""
            #ifdef __cplusplus
        }
        #endif
        #endif
""");

        headerWriter.close();

        writeInts();


        writeDoubles();

        writeStrings();

        writeEnd();

        fileWriter.close();
    }

}