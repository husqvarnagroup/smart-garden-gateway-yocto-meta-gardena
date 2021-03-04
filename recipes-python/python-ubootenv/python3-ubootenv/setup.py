import setuptools

setuptools.setup(
    name='ubootenv',
    version='0.0.1',
    author='Reto Schneider',
    description='Library to access U-Boot environment',
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Lesser General Public License v2 (LGPLv2)",
        "Operating System :: OS Independent"
    ],
    packages=setuptools.find_packages(),
    py_modules=['ubootenv'],
    setup_requires=["cffi>=1.0.0"],
    install_requires=["cffi>=1.0.0"],
    cffi_modules=["build_ubootenv.py:ffibuilder"],
)
