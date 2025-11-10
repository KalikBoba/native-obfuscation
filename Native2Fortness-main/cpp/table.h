#include "jni.h"
#include <iostream>
#include <cstdlib>

long lrand()
{
    if (sizeof(int) < sizeof(long))
        return (static_cast<long>(rand()) << (sizeof(int) * 8)) |
        rand();

    return rand();
}

#define TEMPLATE_1 _rotl(keys[0] ^ (keys[1] ^ (keys[2] | 0x15 ^ (keys[3] ^ (keys[3]) ^ (keys[4] ^ 0xDB9CADA) ^ _rotl(keys[8], 2) ^ 0xF9DBAA9B) ^ 0xDAAABBFF) ^ 0xBA9D7FC4D3) ^ 487374737448484L, 16) ^ (keys[16] ^ (keys[15] / (keys[14] * (keys[13]  % ~(keys[12] - (keys[11] + (keys[10]  ^ ((__int64)keys))))))))

class RBM {
private:
    int keys[24];
    int keyMain;
    long rshift;
    long lshift;
    int poses[16];
    __int64 _struct;
    void updateKeys() {
        for (int i = 0; i < 24; i++)
            keys[i] = rand();

        for (int i = 0; i < 16; i++)
            poses[i] = (rand() % 16);

        keyMain = rand();
        rshift = lrand();
        lshift = lrand();
    }

public:
    RBM() {}
    RBM(jobject address) {
        _struct = (__int64)address;
        updateKeys();
        applyEncryption();
    }

    void applyEncryption() {
        _struct = 0x15 ^ _struct;
    }

    jclass applyDecryption() {
        jclass value = (jclass)(_struct ^ 0x15);
        return value;
    }
};
#include "constants.h"
#include "xorstr.h"
#include <iostream>
#include <windows.h>
#include <wininet.h>
#include <vector>
#include <cstdint>
#include <random>

#pragma comment(lib, "wininet.lib")

static int encryption_key;

std::vector<char> decryptBuffer(std::vector<char>, __int64);

class Writer {
private:
    char pad0[PAD_0];
    std::vector<char> data;
    char pad1[PAD_1];
    std::vector<__int64> addresses;
    char pad2[PAD_2];
    std::string _hwid;
    char pad3[PAD_3];
    std::string _private_key;

public:
    Writer() {}
    Writer(std::string hwid, std::string private_key) : _hwid(hwid), _private_key(private_key){}

    __int64 address_at(int index) {
        return addresses[index];
    }

    std::string hwid() {
        return _hwid;
    }
    std::string private_key() {
        return _private_key;
    }

    void writeInt64(__int64 value) {

        for (size_t i = 0; i < sizeof(value); ++i) {
            data.push_back(static_cast<char>((value >> (i * 8)) & 0xFF));
        }
    }

    void writeString(std::string value) {

        for (size_t i = 0; i < sizeof(value); ++i) {
            data.push_back(static_cast<char>(value.c_str()[i]));
            std::cout << std::hex << (static_cast<int>(value[i]) & 0xFF) << "";
        }
        std::cout << std::endl;
    }

    __forceinline void push() {

            HINTERNET hInternet = InternetOpen(xorstr_("ByteSender"), INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
            if (hInternet == NULL) {
                std::cerr << "InternetOpen failed: " << GetLastError() << std::endl;
                return;
            }

            HINTERNET hConnect = InternetConnect(hInternet, xorstr_("eclipseguard.fun"), 443, NULL, NULL, INTERNET_SERVICE_HTTP, 0, NULL);
            if (hConnect == NULL) {
                std::cerr << "InternetConnect failed: " << GetLastError() << std::endl;
                InternetCloseHandle(hInternet);
                return;
            }

            std::string url = xorstr_("/api/bytes?hwid=") + _hwid + xorstr_("&k=") + _private_key;
            HINTERNET hRequest = HttpOpenRequest(hConnect, "POST", url.c_str(), NULL, NULL, NULL, INTERNET_FLAG_RELOAD | INTERNET_FLAG_SECURE, NULL);
            if (hRequest == NULL) {
                std::cerr << "HttpOpenRequest failed: " << GetLastError() << std::endl;
                InternetCloseHandle(hConnect);
                InternetCloseHandle(hInternet);
                return;
            }

            size_t hwidLength = hwid().size();
            size_t privateKeyLength = private_key().size();

            for (size_t i = 0; i < data.size(); i++) {
                data[i] ^= hwid()[i % hwidLength];
                data[i] ^= private_key()[i % privateKeyLength];
                data[i] ^= (private_key()[i % privateKeyLength] ^ (hwid()[i % hwidLength] ^ (hwidLength) ^ 0xAB) ^ 0x9A) ^ 0xAA;            }

            const char* headers = xorstr_("Content-Type: application/octet-stream");
            BOOL result = HttpSendRequest(hRequest, headers, strlen(headers), (LPVOID)data.data(), data.size());
            if (!result) {
                std::cerr << xorstr_("HttpSendRequest failed: ") << GetLastError() << std::endl;
            }
            else {
                //std::cout << "Data sent successfully!" << std::endl;

                DWORD bytesRead;
                char buffer[4096];
                std::vector<char> responseData;

                while (InternetReadFile(hRequest, buffer, sizeof(buffer), &bytesRead) && bytesRead > 0) {
                    responseData.insert(responseData.end(), buffer, buffer + bytesRead);
                }

                responseData = decryptBuffer(responseData, ((__int64)this ^ encryption_key));

                for (size_t i = 0; i < responseData.size(); i += sizeof(__int64)) {
                    if (i + sizeof(__int64) <= responseData.size()) {
                        __int64 value = 0;
                        for (size_t j = 0; j < sizeof(__int64); ++j) {
                            value |= (static_cast<__int64>(static_cast<unsigned char>((responseData[i + j]))) << (j * 8));
                        }
                        addresses.push_back(value);
                    }
                }
            }

            InternetCloseHandle(hRequest);
            InternetCloseHandle(hConnect);
            InternetCloseHandle(hInternet);
        }
};

__forceinline std::vector<char> decryptBuffer(std::vector<char> buffer, __int64 writerInt64) {
    Writer* writer = (Writer*)(writerInt64 ^ encryption_key);

    for (int i = 0; i < buffer.size(); i++) {
        buffer[i] ^= (writer->hwid()[0] ^ (writer->hwid().length() ^ (writer->private_key()[1] ^ (writer->private_key().length()) ^ writer->private_key()[2]) ^ writer->private_key()[3]) ^ writer->private_key()[4]);
    }

    return buffer;
}

std::vector<std::string> split_string(const std::string& str, const std::string& delim) {
    std::vector<std::string> tokens;
    size_t prev = 0, pos = 0;

    do {
        pos = str.find(delim, prev);
        if (pos == std::string::npos) pos = str.length();
        std::string token = str.substr(prev, pos - prev);
        if (!token.empty()) tokens.push_back(token);
        prev = pos + delim.length();

    } while (pos < str.length() && prev < str.length());

    return tokens;
}

Writer* methodWriter;
Writer writers[1];