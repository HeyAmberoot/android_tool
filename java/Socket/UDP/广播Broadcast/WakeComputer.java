class WakeComputer {


    private void wakeComputer(String port, String macAddr) {

        byte[] mac = getMacBytes(macAddr);
        byte[] magic = new byte[6 + 16 * mac.length];
        //1.写入6个FF
        for (int i = 0; i < 6; i++) {
            magic[i] = (byte) 0xff;
        }
        //2.写入16次mac地址
        for (int i = 6; i < magic.length; i += mac.length) {
            System.arraycopy(mac, 0, magic, i, mac.length);
        }
        new Broadcast(Integer.valueOf(port), magic).start();
    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
