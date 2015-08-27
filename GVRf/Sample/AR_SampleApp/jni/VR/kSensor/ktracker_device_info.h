#ifndef KTRACKER_DEVICE_INFO_H_
#define KTRACKER_DEVICE_INFO_H_

#include <cstdio>

namespace GVR {

/*
namespace KSensor {
enum {
	SecHMTVendorId = 0x2833,
	SecHMTProductId = 0x0010,
};
}
*/
#define HID_MAX_DESCRIPTOR_SIZE		4096

struct hidraw_devinfo {
	__u32 bustype;
	__s16 vendor;
	__s16 product;
};

struct hidraw_report_descriptor {
	__u32 size;
	__u8 value[HID_MAX_DESCRIPTOR_SIZE];
};

// ioctl interface
#define HIDIOCGRDESCSIZE		_IOR('H', 0x01, int)
#define HIDIOCGRDESC			_IOR('H', 0x02, struct hidraw_report_descriptor)
#define HIDIOCGRAWINFO			_IOR('H', 0x03, struct hidraw_devinfo)
#define HIDIOCGRAWNAME(len)		_IOC(_IOC_READ, 'H', 0x04, len)
#define HIDIOCGRAWPHYS(len)		_IOC(_IOC_READ, 'H', 0x05, len)
// The first byte of SFEATURE and GFEATURE is the report number
#define HIDIOCSFEATURE(len)		_IOC(_IOC_WRITE|_IOC_READ, 'H', 0x06, len)
#define HIDIOCGFEATURE(len)		_IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len)

#define HIDRAW_FIRST_MINOR 0
#define HIDRAW_MAX_DEVICES 64
// number of reports to buffer
#define HIDRAW_BUFFER_SIZE 64

}

#endif
